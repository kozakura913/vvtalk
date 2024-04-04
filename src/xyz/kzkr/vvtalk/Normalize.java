package xyz.kzkr.vvtalk;

public class Normalize{
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
