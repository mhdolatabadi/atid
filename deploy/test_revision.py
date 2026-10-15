"""Exercise deploy's public health check without touching Docker or a server."""
import os
from pathlib import Path
import subprocess
import tempfile


source = Path(__file__).with_name("deploy.sh").read_text()
with tempfile.TemporaryDirectory() as directory:
    root = Path(directory)
    (root / "deploy.sh").write_text(source)
    (root / ".env").write_text("ATID_DOMAIN=atid.example.com\n")
    (root / "compose.yaml").write_text("name: atid\n")
    binaries = root / "bin"
    binaries.mkdir()
    mocks = {
        "git": "echo abc123",
        "docker": "exit 0",
        "seq": "echo 1",
        "sleep": "exit 0",
        "curl": r'''case "$*" in
 *-fsSI*) printf 'HTTP/2 200\r\nx-atid-revision: %s\r\n' "$TEST_REVISION" ;;
 *robots.txt*) echo 'Sitemap: https://atid.example.com/sitemap.xml' ;;
 *sitemap.xml*) echo '<loc>https://atid.example.com/app/calendar</loc>' ;;
 *) echo '<link rel="canonical" href="https://atid.example.com/">' ;;
esac''',
    }
    for name, body in mocks.items():
        executable = binaries / name
        executable.write_text("#!/bin/bash\n" + body + "\n")
        executable.chmod(0o755)
    for revision, expected in [("abc123", 0), ("old123", 1), ("", 1)]:
        environment = dict(os.environ, PATH=f"{binaries}:{os.environ['PATH']}",
                           TEST_REVISION=revision)
        result = subprocess.run(["bash", str(root / "deploy.sh"), "--checked-out"],
                                env=environment, capture_output=True, text=True)
        assert result.returncode == expected, result.stdout + result.stderr
        print(f"Revision {revision!r}: expected exit {expected}, passed")

# The image the server pulls must be the one the Images workflow publishes.
repository = Path(__file__).resolve().parent.parent
published = "ghcr.io/mhdolatabadi/atid/web"
assert f'echo "name={published}"' in (repository / ".github/workflows/images.yml").read_text()
assert "${ATID_IMAGE_REPO:-ghcr.io/mhdolatabadi/atid}/web" in (repository / "deploy/compose.yaml").read_text()
print(f"Images workflow and compose agree on {published}")
