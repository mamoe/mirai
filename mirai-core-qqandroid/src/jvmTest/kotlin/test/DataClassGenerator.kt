package test

import kotlinx.coroutines.channels.Channel
import java.lang.StringBuilder
import java.util.*
import java.util.concurrent.BlockingQueue
import kotlin.concurrent.thread


fun main(){
    val inputs = LinkedList<String>()
    thread {
        while (true){
            val x = readLine()
            if(x!=null) inputs.offer(x)
        }
    }

    tailrec fun getNext():String{
        val x = inputs.poll()
        if(x == null){
            Thread.sleep(100)
            return getNext()
        }
        return x;
    }

    fun getAll():String{
        val b = StringBuilder(getNext())
        Thread.sleep(500)
        while(true){
            val c = inputs.poll();
            if(c===null)break;
            b.append("\n").append(c)
        }
        return b.toString();
    }

    while (true){
        println("-proto || -jce")
        val x = getNext()

        if(x.contains("proto",true)){
            //proto
            println("..Copy file content below, after each file is submited, click enter, after all file are in, input \'end'\'")
            val y = mutableListOf<String>()
            while (true){
                val z = getAll()
                if(z.toLowerCase() == "end" || z.toLowerCase() == "end\n"){
                    println("received file content:  " + y.size + ", start generating ProtoBuf" )
                    break;
                }
                y.add(z)
                println("received, ")
            }
            println("======================>protoBuf output<===========================")
            println()
            println()
            println(y.map { it.generateProtoBufDataClass() }.toMutableList().arrangeClasses().joinToString("\n\n"));
            println()
            println()
            println("======================>protoBuf output<===========================")
        }

        if(x.contains("jce",true)){
            println("..Copy the WHOLE file below")
            while (true){
                val z = getAll()
                println("======================>JCE output<===========================")
                println()
                println()
                println(toJCEInfo(z).toString())
                println()
                println()
                println("======================>JCE output<===========================")
                break;
            }
        }
    }
}



