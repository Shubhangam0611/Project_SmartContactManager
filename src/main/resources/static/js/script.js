console.log("this is script file")

const toggleSidebar=()=>{

    if($(".sidebar").is(":visible")){
        //true
        //to be for off the side bar
     
        $(".sidebar").css("display","none");
        $(".content").css("margin-left","0%");
    }else{
        //false
        //when it show
        $(".sidebar").css("display","block");
        $(".content").css("margin-left","20%");

    }

}