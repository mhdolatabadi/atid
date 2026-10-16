// Optional browser regression: run against Vite preview with Playwright installed.
import { chromium } from 'playwright';
const b=await chromium.launch(process.env.CHROME?{executablePath:process.env.CHROME}:{});
try {
const page=await b.newPage();
for (const width of [360, 390, 768]) {
 await page.setViewportSize({width, height:900});
 for (const path of ['/', '/app/prayer-times']) {
  await page.goto((process.env.CHECK_URL ?? 'http://127.0.0.1:4173')+path);
  await page.waitForSelector('.ti-city-picker');
  await page.evaluate(()=>document.fonts.ready);
  await page.locator('.ti-city-picker select').selectOption({label:'بندرعباس'});
  await page.locator('.ti-city-picker').scrollIntoViewIfNeeded();
  await page.waitForTimeout(800);
  for (const colorScheme of ['light', 'dark']) {
   await page.emulateMedia({colorScheme});
   for (const enlarged of [false,true]) {
    const style=enlarged?await page.addStyleTag({content:'.ti-city-picker,.ti-city-picker select,.ti-link-button,.prayer-times-page__location-button{font-size:32px!important}'}):null;
    const geometry=await page.evaluate(()=>{
     const picker=document.querySelector('.ti-city-picker');
     const button=picker.parentElement.querySelector('button');
     const a=picker.getBoundingClientRect(),b=button.getBoundingClientRect(),select=picker.querySelector('select').getBoundingClientRect();
     return {selectWidth:select.width, sameRow:a.bottom>b.top&&b.bottom>a.top, overflow:document.documentElement.scrollWidth>innerWidth, overlap:a.bottom>b.top&&b.bottom>a.top&&Math.min(a.right,b.right)>Math.max(a.left,b.left)+1, outside:Math.min(a.left,b.left,select.left)<-1||Math.max(a.right,b.right,select.right)>innerWidth+1, targets:Math.min(select.height,b.height)>=44};
    });
    console.log(width,path,colorScheme,enlarged,geometry);
    if(geometry.overflow||geometry.overlap||geometry.outside||!geometry.targets)throw Error('Place control bounds');
    if(enlarged&&width<=390&&(geometry.sameRow||geometry.selectWidth<140))throw Error('Large control crowding');

    if(style)await style.evaluate(el=>el.remove());
   }
  }
 }
}
} finally {await b.close();}
