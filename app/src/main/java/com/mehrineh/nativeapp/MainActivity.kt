package com.mehrineh.nativeapp

import android.app.*
import android.content.*
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.*
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var root: LinearLayout
    private lateinit var nav: LinearLayout
    private val prefs by lazy { getSharedPreferences("mehrineh_local", MODE_PRIVATE) }
    private var family = false
    private var page = 0
    private var tts: TextToSpeech? = null
    private val green = Color.rgb(23,107,85)
    private val blue = Color.rgb(37,93,168)
    private val red = Color.rgb(181,43,59)
    private val ink = Color.rgb(26,48,39)
    private val muted = Color.rgb(105,124,115)
    private val voiceRequest = 77
    private val themes get() = intArrayOf(green,blue,red,Color.rgb(34,34,34))
    private val accent get() = themes[prefs.getInt("theme",0).coerceIn(0,3)]
    private val fontScale get() = 1f + prefs.getInt("font",0)*0.15f
    private fun dp(n:Int) = (resources.displayMetrics.density*n).toInt()
    override fun onCreate(savedInstanceState:Bundle?) { super.onCreate(savedInstanceState); window.statusBarColor=Color.rgb(246,248,247); window.navigationBarColor=Color.WHITE; setContentView(R.layout.activity_main); root=findViewById(R.id.content); nav=findViewById(R.id.bottomNav); tts=TextToSpeech(this,this); show() }
    override fun onInit(status:Int) { if(status==TextToSpeech.SUCCESS) tts?.language=Locale("fa","IR") }
    private fun say(s:String) { tts?.speak(s,TextToSpeech.QUEUE_FLUSH,null,"mehrineh") ?: toast("خواندن صوتی در دسترس نیست") }
    private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_LONG).show()
    private fun text(s:String,size:Int=17,bold:Boolean=false,color:Int=ink):TextView = TextView(this).apply { text=s; textSize=size*fontScale; setTextColor(color); gravity=Gravity.RIGHT; if(bold) setTypeface(null,Typeface.BOLD); setLineSpacing(dp(3).toFloat(),1f) }
    private fun bg(color:Int=Color.WHITE,stroke:Int=Color.rgb(232,237,235),radius:Int=20):GradientDrawable=GradientDrawable().apply { setColor(color); cornerRadius=dp(radius).toFloat(); setStroke(dp(1),stroke) }
    private fun col():LinearLayout=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; layoutDirection=View.LAYOUT_DIRECTION_RTL }
    private fun row():LinearLayout=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; layoutDirection=View.LAYOUT_DIRECTION_RTL; gravity=Gravity.CENTER_VERTICAL }
    private fun add(v:View,margin:Int=9) { root.addView(v,LinearLayout.LayoutParams(-1,-2).apply { bottomMargin=dp(margin) }) }
    private fun section(s:String) { add(text(s,21,true),12) }
    private fun card(title:String,desc:String,icon:Int=R.drawable.ic_grid,action:()->Unit) {
        val v=layoutInflater.inflate(R.layout.item_tile,root,false)
        v.findViewById<TextView>(R.id.tileTitle).apply { text=title; textSize=17*fontScale }
        v.findViewById<TextView>(R.id.tileDetail).apply { text=desc; textSize=12*fontScale }
        v.findViewById<ImageView>(R.id.tileIcon).apply { setImageResource(icon); setColorFilter(accent) }
        v.setOnClickListener { action() }; add(v)
    }
    private fun button(label:String,action:()->Unit,solid:Boolean=true):TextView=text(label,16,true,if(solid) Color.WHITE else accent).apply {
        gravity=Gravity.CENTER; minHeight=dp(56); setPadding(dp(12),dp(10),dp(12),dp(10)); background=bg(if(solid) accent else Color.WHITE,accent,16); setOnClickListener { action() }
    }
    private fun edit(hint:String):EditText=EditText(this).apply { this.hint=hint; textSize=17*fontScale; setSingleLine(true); setPadding(dp(14),dp(12),dp(14),dp(12)); background=bg(); layoutDirection=View.LAYOUT_DIRECTION_RTL }
    private fun show() {
        root.removeAllViews(); nav.removeAllViews(); findViewById<TextView>(R.id.switchRole).apply { text=if(family) "حالت سالمند" else "پنل خانواده"; setTextColor(accent); setOnClickListener { family=!family;page=0;show() } }
        findViewById<TextView>(R.id.brand).setTextColor(accent)
        if(family) familyPage() else when(page) {0->home();1->reminders();2->lessons();3->assistant();4->shortcuts();5->settings();else->home()}
        val tabs=if(family) listOf(Triple("خانه",R.drawable.ic_users,0),Triple("یادآورها",R.drawable.ic_calendar,1),Triple("تنظیمات",R.drawable.ic_settings,5)) else listOf(Triple("خانه",R.drawable.ic_grid,0),Triple("یادآورها",R.drawable.ic_calendar,1),Triple("آموزش",R.drawable.ic_book,2),Triple("دستیار",R.drawable.ic_chat,3))
        for((label,icon,id) in tabs) {
            val box=col().apply { gravity=Gravity.CENTER; setPadding(dp(4),dp(7),dp(4),dp(7)); setOnClickListener { page=id;show() } }
            val img=ImageView(this).apply { setImageResource(icon); setColorFilter(if(page==id) accent else muted); layoutParams=LinearLayout.LayoutParams(dp(25),dp(25)) }
            box.addView(img); box.addView(text(label,11,page==id,if(page==id) accent else muted).apply { gravity=Gravity.CENTER })
            nav.addView(box,LinearLayout.LayoutParams(0,-1,1f))
        }
    }
    private fun home() {
        add(text("سلام، خوش آمدید",27,true),4);add(text("امروز هم کنار هم، با خیال راحت",15,false,muted),22)
        val hero=col().apply { background=bg(Color.rgb(231,245,239),Color.rgb(231,245,239));setPadding(dp(20),dp(20),dp(20),dp(20)) }
        hero.addView(text("امروز چطور هستید؟",22,true,accent));hero.addView(text("با یک لمس، خانواده‌تان را باخبر کنید.",14,false,muted))
        hero.addView(button("حالم خوبه  ·  ثبت اعلام حال",{ prefs.edit().putLong("checkin",System.currentTimeMillis()).apply();toast("اعلام حال ثبت شد (فقط روی همین دستگاه)");show() }).apply { val lp=LinearLayout.LayoutParams(-1,-2);lp.topMargin=dp(15);layoutParams=lp });add(hero,20)
        section("تماس سریع با عزیزان")
        val names=listOf("دخترم","پسرم","همسرم");val nums=listOf("phone1","phone2","phone3")
        for(i in names.indices) card(names[i],if(prefs.getString(nums[i],"").isNullOrEmpty()) "شماره را در تنظیمات ثبت کنید" else "برای تماس لمس کنید",R.drawable.ic_phone) { val n=prefs.getString(nums[i],"").orEmpty();if(n.isBlank()) {page=5;show()} else startActivity(Intent(Intent.ACTION_DIAL,Uri.parse("tel:${Uri.encode(n)}"))) }
        section("دسترسی سریع")
        card("برنامه‌های من","اسنپ، فیلیمو، بادصبا و برنامه‌های دیگر",R.drawable.ic_grid){page=4;show()}
        card("درخواست کمک","ثبت درخواست برای مشاهده در پنل خانواده",R.drawable.ic_heart){ prefs.edit().putLong("help",System.currentTimeMillis()).apply();toast("درخواست کمک روی همین دستگاه ثبت شد؛ هنوز اعلان از راه دور ندارد") }
        card("تنظیمات شخصی","رنگ، اندازه نوشته و شماره تماس‌ها",R.drawable.ic_settings){page=5;show()}
    }
    private fun reminders() {
        section("تقویم و یادآورها")
        add(text(SimpleDateFormat("EEEE، d MMMM yyyy",Locale("fa","IR")).format(Date()),15,false,muted))
        val input=edit("مثلاً ساعت ۸ دارو بخورم");add(input)
        val time=TimePicker(this).apply { setIs24HourView(true) };add(time)
        add(button("افزودن یادآوری",{
            val title=input.text.toString().trim();if(title.isBlank()){toast("متن یادآوری را وارد کنید");return@button}
            val c=Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY,time.hour);set(Calendar.MINUTE,time.minute);set(Calendar.SECOND,0);if(timeInMillis<=System.currentTimeMillis())add(Calendar.DAY_OF_YEAR,1) }
            val list=prefs.getString("reminders","").orEmpty();prefs.edit().putString("reminders",list+"${c.timeInMillis}|${title.replace("|"," ").replace("\n"," ")}\n").apply()
            toast("یادآوری ذخیره شد. برای خواندن صوتی روی آن بزنید.");show()
        }))
        section("یادآوری‌های ثبت‌شده")
        val lines=prefs.getString("reminders","").orEmpty().lines().filter{it.contains('|')}
        if(lines.isEmpty()) add(text("هنوز یادآوری ثبت نشده است.",15,false,muted))
        for(line in lines.reversed()) { val p=line.split('|',limit=2);val t=p[0].toLongOrNull()?:continue;card(p[1],SimpleDateFormat("HH:mm — yyyy/MM/dd",Locale("fa","IR")).format(Date(t))+"  ·  برای شنیدن لمس کنید",R.drawable.ic_bell){say(p[1])} }
        add(text("یادآورها در این نسخه داخل برنامه ذخیره می‌شوند؛ اعلان زمان‌بندی‌شده سیستم هنوز فعال نیست.",13,false,muted))
    }
    private fun lessons() {
        section("یادگیری قدم‌به‌قدم")
        add(text("آموزش تصویری و تمرین تعاملی برای کارهای روزمره",15,false,muted))
        card("آموزش خرید اینترنتی","تمرین امن و بدون پرداخت واقعی",R.drawable.ic_cart){ shoppingLesson() }
        card("سفر با اسنپ","آشنایی با مراحل درخواست خودرو",R.drawable.ic_book){ simpleLesson("درخواست خودرو","۱. مبدأ را انتخاب کنید.\n۲. مقصد را وارد کنید.\n۳. هزینه را بررسی کنید.\n۴. مشخصات راننده را تطبیق دهید.") }
        card("امنیت خرید آنلاین","آشنایی با لینک‌های مشکوک و پرداخت امن",R.drawable.ic_book){simpleLesson("خرید امن","آدرس فروشگاه را بررسی کنید.\nرمز کارت را به دیگران ندهید.\nروی لینک‌های ناشناس کلیک نکنید.")}
        add(text("محتوای ویدئویی واقعی باید به پروژه اضافه شود؛ این نسخه تمرین بومی اندروید را ارائه می‌دهد.",13,false,muted))
    }
    private fun simpleLesson(title:String,body:String){root.removeAllViews();section(title);add(text(body,19));add(button("برای من بخوان",{say(body)}));add(button("بازگشت",{show()},false))}
    private fun shoppingLesson(){root.removeAllViews();section("تمرین خرید اینترنتی");add(text("مرحله ۱ از ۳: محصول را انتخاب کنید.",17));card("برنج ایرانی","بسته آزمایشی — ۲ کیلوگرم",R.drawable.ic_cart){root.removeAllViews();section("مرحله ۲ از ۳");add(text("سبد خرید شما: برنج ایرانی، ۲ کیلوگرم",18));add(button("تأیید سبد",{root.removeAllViews();section("مرحله ۳ از ۳");add(text("این یک تمرین است و هیچ پولی پرداخت نمی‌شود.",18));add(button("ثبت سفارش تمرینی",{prefs.edit().putString("lastOrder","برنج ایرانی، ۲ کیلوگرم").apply();toast("تمرین با موفقیت انجام شد");show()}))}))};add(button("بازگشت",{show()},false))}
    private fun assistant() {
        section("همراه هوشمند مهرینه")
        add(text("در این نسخه، دستیار فرمان‌های مشخص را روی گوشی اجرا می‌کند؛ به سرویس هوش مصنوعی آنلاین متصل نیست.",14,false,muted))
        val history=prefs.getString("chat","").orEmpty();if(history.isNotBlank()) add(text(history,16).apply { setPadding(dp(15),dp(15),dp(15),dp(15));background=bg() })
        val input=edit("مثلاً: دو کیلو گوشت بخر");add(input)
        add(button("ارسال پیام",{respond(input.text.toString())}))
        add(button("صحبت با میکروفن",{try { val intent=Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply { putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);putExtra(RecognizerIntent.EXTRA_LANGUAGE,"fa-IR");putExtra(RecognizerIntent.EXTRA_PROMPT,"درخواستتان را بگویید") };startActivityForResult(intent,voiceRequest) }catch(e:Exception){toast("تشخیص گفتار روی این گوشی در دسترس نیست")}},false))
        card("نمونه درخواست","دو کیلو گوشت بخر",R.drawable.ic_cart){respond("دو کیلو گوشت بخر")}
    }
    @Deprecated("Android speech result callback")
    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?) {super.onActivityResult(requestCode,resultCode,data);if(requestCode==voiceRequest && resultCode==RESULT_OK){val spoken=data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty();respond(spoken)}}
    private fun respond(query:String){if(query.isBlank())return;val answer=when {query.contains("گوشت")||query.contains("بخر")||query.contains("خرید")->"درخواست شما را متوجه شدم. برای ثبت سفارش آزمایشی، دکمه تأیید را بزنید.";query.contains("یادآور")||query.contains("دارو")->"برای تنظیم یادآوری، بخش تقویم و یادآورها را باز کنید.";query.contains("کمک")->"برای درخواست کمک، از دکمه درخواست کمک در صفحه خانه استفاده کنید.";else->"من می‌توانم درباره خرید آزمایشی، یادآوری و درخواست کمک راهنمایی کنم."}
        prefs.edit().putString("chat","شما: $query\nمهرینه: $answer").apply();say(answer);show()
        if(query.contains("گوشت")||query.contains("بخر")||query.contains("خرید")) add(button("تأیید ثبت سفارش آزمایشی",{prefs.edit().putString("lastOrder",query).apply();toast("سفارش آزمایشی ثبت شد؛ خرید واقعی انجام نشده است");show()}))
    }
    private fun shortcuts(){section("برنامه‌های پرکاربرد");for((label,pkg) in listOf("اسنپ" to "cab.snapp.passenger","فیلیمو" to "com.aparat.filimo","بادصبا" to "com.mobiliha.badesaba","واتساپ" to "com.whatsapp")) card(label,"باز کردن برنامه نصب‌شده",R.drawable.ic_grid){try{val i=packageManager.getLaunchIntentForPackage(pkg);if(i!=null)startActivity(i) else toast("این برنامه نصب نیست یا در دسترس نیست")}catch(e:Exception){toast("باز کردن برنامه ممکن نشد")}} }
    private fun settings(){section("شخصی‌سازی مهرینه");add(text("چهار تم رنگی",17,true));for((i,label) in listOf("سبز آرامش","آبی آسمانی","قرمز مهر","کنتراست بالا").withIndex())add(button(label,{prefs.edit().putInt("theme",i).apply();show()},i==prefs.getInt("theme",0)))
        section("اندازه نوشته");for(i in 0..3)add(button(listOf("معمولی","بزرگ","خیلی بزرگ","بسیار بزرگ")[i],{prefs.edit().putInt("font",i).apply();show()},i==prefs.getInt("font",0)))
        section("شماره تماس عزیزان");for((i,label) in listOf("دخترم","پسرم","همسرم").withIndex()){val key="phone${i+1}";val field=edit("شماره $label").apply{setText(prefs.getString(key,""))};add(field);add(button("ذخیره شماره $label",{prefs.edit().putString(key,field.text.toString().trim()).apply();toast("ذخیره شد")},false))}
        add(button("آزمایش صدای فارسی",{say("سلام. به مهرینه خوش آمدید.")}))
    }
    private fun familyPage(){if(page==5){settings();return};if(page==1){reminders();return};section("پنل خانواده");add(text("نسخه نمایشی محلی — داده‌ها فقط روی همین گوشی دیده می‌شوند.",14,false,muted))
        val last=prefs.getLong("checkin",0);card("آخرین اعلام حال",if(last==0L)"هنوز ثبت نشده" else SimpleDateFormat("yyyy/MM/dd - HH:mm",Locale("fa","IR")).format(Date(last)),R.drawable.ic_heart){toast("این وضعیت فقط روی همین گوشی ثبت شده است")}
        val help=prefs.getLong("help",0);card("درخواست کمک",if(help==0L)"درخواستی ثبت نشده" else "درخواست کمک ثبت‌شده روی این دستگاه",R.drawable.ic_bell){if(help!=0L)AlertDialog.Builder(this).setTitle("درخواست کمک").setMessage("این درخواست از راه دور ارسال نشده است.").setPositiveButton("متوجه شدم",null).show()}
        card("آخرین سفارش",prefs.getString("lastOrder","سفارشی ثبت نشده").orEmpty(),R.drawable.ic_cart){ }
        card("مدیریت یادآورها","افزودن و شنیدن یادآوری‌های سالمند",R.drawable.ic_calendar){page=1;show()}
        card("تنظیمات","تم و اندازه متن",R.drawable.ic_settings){page=5;show()}
    }
    override fun onDestroy(){tts?.stop();tts?.shutdown();super.onDestroy()}
}
