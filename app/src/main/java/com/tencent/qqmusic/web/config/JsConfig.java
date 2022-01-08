package com.tencent.qqmusic.web.config;

public class JsConfig {
    private static final String if_format = "if(document.location.href.indexOf('%s')!=-1){\n" +
            "%s" +
            "\n}\n";

    private static final String else_if_format = "else " + if_format;

    private static final String find_by_class_format = "\tdocument.getElementsByClassName('%s')[0]";
    private static final String find_by_class_style_format = find_by_class_format + ".style%s";
    private static final String find_by_class_innerText_format = find_by_class_format + ".innerText";

    private static final String visibility_format = ".visibility = '%s';\n";
    private static final String margin_top_format = ".marginTop = '%s';\n";

    public static final String taogeNextOrPreJs =  "if(document.location.href.indexOf('taoge.html')!=-1){\n" +
            "    var items = document.getElementsByClassName('qui_list__item');\n" +
            "    var item;\n" +
            "    for(var i=0;i<items.length;i++){\n" +
            "      if(items[i].className == 'qui_list__item current'){\n" +
            "          item = items[i %s 1];\n" +
            "          break;\n" +
            "      }\n" +
            "    }\n" +
            "\n" +
            "    var rect = item.getBoundingClientRect(); \n" +
            "    var touch = new Touch({\"identifier\" : 0,\"target\" : item});\n" +
            "\n" +
            "    var touchstart = new TouchEvent(\"touchstart\", {\n" +
            "          cancelable: true,\n" +
            "          bubbles: true,\n" +
            "          composed: true,\n" +
            "          touches: [touch],\n" +
            "          targetTouches: [touch],\n" +
            "          changedTouches: [touch]\n" +
            "       });\n" +
            "\n" +
            "    var touchend = new TouchEvent(\"touchend\", {\n" +
            "          cancelable: true,\n" +
            "          bubbles: true,\n" +
            "          composed: true,\n" +
            "          touches: [touch],\n" +
            "          targetTouches: [touch],\n" +
            "          changedTouches: [touch]\n" +
            "       });\n" +
            "\n" +
            "    item.dispatchEvent(touchstart);\n" +
            "    item.dispatchEvent(touchend);\n" +
            "}\n";

    public static final String playsongNextOrPreJs = "if(document.location.href.indexOf('playsong.html')!=-1){\n" +
            "    var items = document.getElementsByClassName('play_list__item');\n" +
            "    var item;\n" +
            "    for(var i=0;i<items.length;i++){\n" +
            "      if(items[i].className == 'play_list__item current'){\n" +
            "          item = items[i %s 1];\n" +
            "          break;\n" +
            "      }\n" +
            "    }\n" +
            "\n" +
            "    item.click();\n" +
            "}";

    public static String build(String packageName) {
        String js = String.format(if_format, "m/index.html"
                , String.format(find_by_class_style_format, "header", String.format(margin_top_format, "30px"))
                        + String.format(find_by_class_style_format, "top_operation_bar", String.format(margin_top_format, "30px"))
                        +String.format(find_by_class_style_format,"top_operation_bar__tit",String.format(margin_top_format, "15px"))
                        + String.format(find_by_class_style_format, "top_operation_bar__txt", String.format(visibility_format, "hidden"))
                        + String.format(find_by_class_style_format, "top_operation_bar__btn", String.format(visibility_format, "hidden"))
                        + String.format(find_by_class_style_format, "bottom_bar", String.format(visibility_format, "hidden")));
        js += String.format(else_if_format, "toplist.html"
                , String.format(find_by_class_style_format, "top_operation_bar", String.format(visibility_format, "hidden")));
        String playsong_format_html = String.format(find_by_class_style_format, "top_operation_bar", String.format(visibility_format, "hidden"))
                + String.format(find_by_class_style_format, "btn_download", String.format(visibility_format, "hidden"))
                + String.format(find_by_class_style_format, "flotage", String.format(visibility_format, "hidden"))
                + String.format(find_by_class_style_format, "mod_more_comment", String.format(visibility_format, "hidden"))
                + String.format(find_by_class_style_format, "bottom_operation_box", String.format(visibility_format, "hidden"))
                + String.format(find_by_class_style_format, "wrap", String.format(margin_top_format, "-60px"))
                + String.format(find_by_class_style_format, "song_info", String.format(margin_top_format, "50px"))
                + String.format(find_by_class_style_format, "main", ".height = '5rem';\n")
                + String.format(find_by_class_style_format, "song_info__bd", ".height = '3rem';\n")
                + String.format("\tvar songname = %s;\n" +
                        "\tvar singername = %s;\n" +
                        "\tconsole.log('" + packageName + ":songname:'+songname);\n" +
                        "\tconsole.log('" + packageName + ":singername:'+singername);\n"
                , String.format(find_by_class_innerText_format, "song_name__text")
                , String.format(find_by_class_innerText_format, "singer_name"))
                + "\tvar audios = document.getElementsByTagName('audio');\n" +
                "\tconsole.log('" + packageName + ":progress:'+ audios[audios.length - 1].currentTime);\n" +
                "\tconsole.log('" + packageName + ":total:'+ audios[audios.length - 1].duration);\n" +
                "\tconsole.log('" + packageName + ":playstate:'+ (audios[audios.length - 1].paused?'pause':'play'));\n" +
                "\tconsole.log('" + packageName + ":cover:'+document.getElementsByClassName('bg__img')[0].src);\n" +
                "\tconsole.log('" + packageName + ":lyric:'+document.getElementsByClassName('lyric__para current')[0].innerText);\n"
                + String.format("\tvar mvname = %s+' - '+%s;\n\tconsole.log('" + packageName + ":mvname:'+mvname);"
                , String.format(find_by_class_innerText_format, "mv__tit")
                , String.format(find_by_class_innerText_format, "mv__desc"))
                + String.format(find_by_class_style_format, "lyric__bd", ".height = '2.8rem';\n")
                + String.format(find_by_class_style_format, "kg", String.format(visibility_format, "hidden"))
                + String.format(find_by_class_style_format, "lyric_action", String.format(visibility_format, "hidden"));

        js += String.format(else_if_format, "playsong.html"
                , playsong_format_html);

        js += String.format(else_if_format, "playsong/index.html"
                , playsong_format_html);
        js += String.format(else_if_format, "taoge.html"
                , String.format(find_by_class_style_format, "top_operation_bar", String.format(visibility_format, "hidden"))
                        + String.format(find_by_class_style_format, "top_wrap", String.format(margin_top_format, "-60px"))

                        + "\tvar audios = document.getElementsByTagName('audio');\n" +
                        "\tconsole.log('" + packageName + ":progress:'+ audios[audios.length - 1].currentTime);\n" +
                        "\tconsole.log('" + packageName + ":total:'+ audios[audios.length - 1].duration);\n" +
                        "\tconsole.log('" + packageName + ":playstate:'+ (audios[audios.length - 1].paused?'pause':'play'));\n" +
                        "\tconsole.log('" + packageName + ":cover:'+document.getElementsByClassName('top_wrap__bg')[0].src);\n" +
                        "\tconsole.log('" + packageName + ":lyric:'+document.getElementsByClassName('lyrics__item current')[0].innerText);\n" +
                        String.format("var singer_and_song = %s.getElementsByClassName('qui_list__txt')[1].innerText;\n" +
                                        "\tvar singer = singer_and_song.split('·')[0];\n" +
                                        "\tvar song = singer_and_song.split('·')[1];\n" +
                                        "\tconsole.log('com.tencent.qqmusic.web:songname:'+song);\n" +
                                        "\tconsole.log('com.tencent.qqmusic.web:singername:'+singer);\n"
                                , String.format(find_by_class_format, "qui_list__item current")));
        return js;
    }
}
