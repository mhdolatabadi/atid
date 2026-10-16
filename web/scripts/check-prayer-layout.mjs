// Run with Vite preview and Playwright installed locally.
import { chromium } from 'playwright';
const b = await chromium.launch(process.env.CHROME ? {executablePath:process.env.CHROME} : {});
try {
const page = await b.newPage();
for(const width of [360,390,768]) {
 await page.setViewportSize({width,height:900});
 for(const path of ['/', '/app/prayer-times']) {
  await page.goto((process.env.CHECK_URL ?? 'http://127.0.0.1:4173')+path); await page.evaluate(()=>document.fonts.ready);
  await page.waitForSelector('.ti-prayer__row, .prayer-times-row'); await page.locator('.ti-prayer__row, .prayer-times-row').first().scrollIntoViewIfNeeded(); await page.waitForTimeout(800);
  for(const scheme of ['light','dark']) {
   await page.emulateMedia({colorScheme:scheme});
   for(const large of [false,true]) {
    const style=large?await page.addStyleTag({content:'.ti-prayer__row,.prayer-times-row{font-size:32px!important}'}):null;
    const result=await page.evaluate(()=>({overflow:document.documentElement.scrollWidth>innerWidth, collision:[...document.querySelectorAll('.ti-prayer__row,.prayer-times-row')].some(row=>{const [a,b]=[...row.children].map(c=>c.getBoundingClientRect());return Math.min(a.right,b.right)>Math.max(a.left,b.left)+1;})}));
    console.log(width,path,scheme,large,result);if(result.overflow||result.collision)throw Error('Prayer row layout');
    if(style)await style.evaluate(el=>el.remove());
   }

  }
 }
}
} finally { await b.close(); }
