import java.io.File

fun main(args:Array<String>){
    if(args.size!=2){
        println("Usage: Reorganizer flatten|deepen directory")
        System.exit(1)
    }
    val dir= File(args[1])
    if(!dir.exists()|| !dir.isDirectory){
        println("$dir does not exist or is not a directory")
        System.exit(2)
    }
    val su=args[0].substring(0,2)
    when(args[0].substring(0,2)){
        "fl"->flatten(dir)
        "de"->deepen(dir)
        "cl"->cleanmeta(dir)
        else->{
            println("Command $args[0] not found. Use flatten or deepen")
            System.exit(3)
        }
    }
}

fun flatten(dir:File){
    dir.listFiles().forEach { coll ->
        if(coll.isDirectory && coll.length()>2 && !coll.startsWith(".")){
            coll.listFiles().forEach { subdir ->
                subdir.renameTo(File(dir,subdir.name))
            }
            coll.delete()
        }
    }
}

fun deepen(dir:File){
    dir.listFiles().forEach { subdir ->
        if(subdir.isDirectory && subdir.name.length>2){
            val coll=File(dir,subdir.name.substring(0,2))
            if(!coll.exists()){
                coll.mkdir()
            }
            subdir.renameTo(File(coll,subdir.name))
        }

    }
}

fun cleanmeta(dir:File){
    dir.listFiles().forEach { file ->
        if(file.isDirectory){
            cleanmeta(file)
        }else{
            if(file.name.endsWith(".meta")){
                file.delete()
            }
        }
    }
}