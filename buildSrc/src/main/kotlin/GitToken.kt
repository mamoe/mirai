

fun getGitToken():String{
    with(System.getProperty("user.dir") + "/token.txt"){
        println("reading token file in " + this)
        return File(this).readText()
    }
}