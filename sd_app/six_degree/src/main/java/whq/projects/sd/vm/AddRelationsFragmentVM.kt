package whq.projects.sd.vm

import android.app.Application
import android.text.Editable
import androidx.lifecycle.MutableLiveData
import whq.projects.entities.TOGGLE

class AddRelationsFragmentVM(application: Application) : SdFragmentVM(application) {
    val relations: MutableLiveData<Editable> = MutableLiveData()
    val toggle: MutableLiveData<TOGGLE> = MutableLiveData()
    val helpString = """
在这里指定两部分数据：人的特征和他们的关系.
关系：形如“林如海 女儿 林黛玉”,用空白隔成3部分,首尾均为名称,中间的部分是关系名,当前支持如下关系名：
爷爷,奶奶,姥姥,姥爷,爸爸,岳父,姑姑,叔叔,伯伯,妈妈,岳母,阿姨,舅舅,妻子,丈夫,哥哥,姐姐,弟弟,兄弟,妹妹,堂哥,堂姐,堂弟,堂妹,表哥,表姐,表弟,表妹,儿子,女儿,侄子,侄女,外甥,外甥女,孙子,孙女,外孙子,外孙女,普通朋友,生意朋友,恋爱朋友,现在同事,曾经同事,幼儿园同学,小学同学,初中同学,高中同学,大学同学,硕士同学,博士同学,其他,服侍,启蒙老师,同学,同事,亲属,朋友,古典,老师
此外,系统还支持如下形式的关系数据，即可以给同学关系、同事关系加上“单位”和“年份”的属性
司徒寰宇 大学同学:单位=北京大学,年份=1999 上官会敏
人的特征： 形如“司徒寰宇:手机号=11100000889,<特征>=<特征值>”,其中,"："前是名称,其后是“<特征>=<特征值>”形式的特征描述,用“,”分隔多个特征，且“手机号”是必填项,当前支持的“特征”有：
备注,手机号/手机,昵称,住址,籍贯,工作在,就读在,幼儿园,小学,中学/初中,高中,大学,研究生/硕士,博士,幼儿园入学,小学入学,中学入学/初中入学,高中入学,大学入学,研究生入学/硕士入学,博士入学,工作单位,曾经工作在/原工作单位,当前单位入职于,原单位入职于
说明:六度类似于互联网、物联网,人就类似于互联网中的计算机、物联网中的实体,壮大这个六度的网络需要每个您的参与,您的奉献与回报是成正比的～
    """.trimIndent()
}