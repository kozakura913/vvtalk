package xyz.kzkr.vvtalk;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Normalize{

	public static String urlcut(String text){
		//URL省略処理
		//URL判定基準を正規表現で指定
		Matcher m=Pattern.compile("https?://\\S++").matcher(text);
		m.reset();
		boolean result=m.find();
		if(result){
			int co=0;//URLの数
			do{
				co++;
				result=m.find();
			}while(result);
			m.reset();
			result=m.find();
			boolean b=true;
			StringBuffer sb=new StringBuffer();
			do{
				if(b){//初回
					b=false;
					if(co==1) m.appendReplacement(sb,"URL省略");//対象が一つの時
					else m.appendReplacement(sb,co+"件のURLを省略");
				}else m.appendReplacement(sb,"");//2回目以降
				result=m.find();
			}while(result);
			m.appendTail(sb);
			text=sb.toString();
		}
		return text;
	}
	public static String filename(String g) {
		String r="ファイル";
		if(g.endsWith(".png")||g.endsWith(".gif")||g.endsWith(".jpg")||g.endsWith(".jpeg")
				||g.endsWith(".webp")||g.endsWith(".bmp")||g.endsWith(".tiff")||g.endsWith(".bmp")||
				g.endsWith(".xcf")||g.endsWith(".clip")||g.endsWith(".psd")||g.endsWith(".svg")){
			r="画像";
		}else if(g.endsWith(".txt")){
			r="テキストファイル";
		}else if(g.endsWith(".rs")||g.endsWith(".js")||g.endsWith(".java")||g.endsWith(".ts")||g.endsWith(".py")||g.endsWith(".cs")||g.endsWith(".c")){
			r="ソースファイル";
		}else if(g.endsWith(".class")){
			r="クラスファイル";
		}else if(g.endsWith(".h")){
			r="ヘッダーファイル";
		}else if(g.endsWith(".ini")||g.endsWith(".xml")||g.endsWith(".json")){
			r="データファイル";
		}else if(g.endsWith(".css")||g.endsWith(".scss")){
			r="スタイルシート";
		}else if(g.endsWith(".html")){
			r="エイチティーエムエル";
		}else if(g.endsWith(".av1")||g.endsWith(".mp4")||g.endsWith(".avi")||g.endsWith(".mov")||g.endsWith(".flv")){
			r="動画";
		}else if(g.endsWith(".opus")||g.endsWith(".ogg")||g.endsWith(".wav")||g.endsWith(".mp3")||g.endsWith(".mid")||g.endsWith(".midi")){
			r="音楽";
		}else if(g.endsWith(".zip")||g.endsWith(".gz")||g.endsWith(".7z")||g.endsWith(".xz")||g.endsWith(".lzh")){
			r="圧縮ファイル";
		}else if(g.endsWith(".tar")){
			r="アーカイブファイル";
		}else if(g.endsWith(".log")){
			r="ログファイル";
		}else{
			int li=g.lastIndexOf('.');
			if(li>=0&&li+1<g.length()){
				System.out.println("未定義ファイル"+g);
				String s=g.substring(li+1);
				char[] ca=new char[s.length()*2];
				int j=0;
				for(int i=0;i<ca.length;i+=2){
					ca[i]=s.charAt(j);
					ca[i+1]=',';
					j++;
				}
				r=String.valueOf(ca)+"ファイル";
			}
		}
		return r;
	}
}
