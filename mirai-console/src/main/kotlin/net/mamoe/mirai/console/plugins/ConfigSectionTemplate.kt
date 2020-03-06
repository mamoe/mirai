package net.mamoe.mirai.console.plugins

interface ConfigSectionTemplate{


    fun autoSave(){

    }


}



class MyConfigObject:ConfigSectionTemplate {

    class MyTWClass : ConfigSectionTemplate {

    }

}