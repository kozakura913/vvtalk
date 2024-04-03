package xyz.kzkr.vvtalk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.audio.hooks.ConnectionListener;
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.managers.AudioManager;

public class VoiceSender implements AudioSendHandler, ConnectionListener{
	public long textChannel;
	private VoiceChannel vc;
	private AudioManager am;
	private boolean stop;
	private ArrayDeque<String> queue;
	public int speaker;
	private ByteArrayIOBuffer buffer=new ByteArrayIOBuffer(new byte[4096]);
	private byte[] data_buffer;
	private ByteBuffer raw_buffer;
	public double speedScale=1,pitchScale=0;
	ByteArrayOutputStream baos=new ByteArrayOutputStream();

	public VoiceSender(AudioChannelUnion voice_channel){
		queue=new ArrayDeque<String>();
		int frames20Ms=(int) (INPUT_FORMAT.getFrameRate()*0.02);
		int buffer_size=INPUT_FORMAT.getFrameSize()*frames20Ms;
		data_buffer=new byte[buffer_size];
		raw_buffer=ByteBuffer.wrap(data_buffer);
		vc=voice_channel.asVoiceChannel();
		am=voice_channel.getGuild().getAudioManager();
		am.setSendingHandler(this);
		am.setConnectionListener(this);
		am.openAudioConnection(vc);
		new Thread(this::mainLoop,"VoiceSenderLoop-"+vc.getId()).start();
	}
	public void insertQueue(String s){
		if(stop)return;
		if(s.isEmpty())return;
		queue.addLast(s);
	}
	private void mainLoop() {
		try{
			while(!stop) {
				if(queue.size()<1) {
					Thread.sleep(10);
					continue;
				}
				String s=queue.pop();
				if(s.isEmpty())break;
				System.out.println("読み上げ対象 "+s);
				speak(s);
			}
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		System.out.println("ループ脱出による切断");
		stop=true;
		queue.clear();
		am.closeAudioConnection();
	}
	public void exit(){
		stop=true;
		queue.clear();
		queue.addLast("");
	}
	@Override
	public void onPing(long ping){
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onStatusChange(ConnectionStatus status){
		if(!am.isConnected()) {
			exit();
		}
	}
	@Override
	public boolean canProvide(){
		try{
			return buffer.getInputStream().available()>0;
		}catch(IOException e){
			e.printStackTrace();
			return false;
		}
	}
	@Override
	public ByteBuffer provide20MsAudio(){
		try{
			readFill(buffer.getInputStream(),data_buffer);
		}catch(IOException e){
			e.printStackTrace();
			Arrays.fill(data_buffer,(byte)0);
		}
		return raw_buffer;
	}

	public static void readFill(InputStream is,byte[] bs) throws IOException{
		int pos=0;
		while(pos<bs.length) {
			int len=is.read(bs,pos,bs.length-pos);
			if(len<0)break;
			pos+=len;
		}
		Arrays.fill(bs,pos,bs.length,(byte)0);
	}
	@SuppressWarnings("unchecked")
	private void editOptions(JSONObject json) {
		json.put("speedScale",Double.valueOf(speedScale));
		json.put("pitchScale",Double.valueOf(pitchScale));
	}
	private void speak(String text) {
		String voicevox_host=System.getProperty("voicevox");
		try{
			text=URLEncoder.encode(text,StandardCharsets.UTF_8);
			URL audio_query_url=URI.create(voicevox_host+"audio_query?speaker="+speaker+"&text="+text).toURL();
			HttpURLConnection audio_query=(HttpURLConnection) audio_query_url.openConnection();
			audio_query.setRequestMethod("POST");
			InputStream query=audio_query.getInputStream();
			try {
				URL synthesis_url=URI.create(voicevox_host+"synthesis?speaker="+speaker).toURL();
				HttpURLConnection synthesis=(HttpURLConnection) synthesis_url.openConnection();
				synthesis.setRequestMethod("POST");
				synthesis.setRequestProperty("Content-Type","application/json");
				synthesis.setDoOutput(true);
				OutputStream os=synthesis.getOutputStream();
				try{
					baos.reset();
					query.transferTo(baos);
					JSONObject json=(JSONObject) new JSONParser().parse(new StringReader(baos.toString(StandardCharsets.UTF_8)));
					this.editOptions(json);
					os.write(json.toJSONString().getBytes(StandardCharsets.UTF_8));
					//query.transferTo(os);
				}finally {
					os.close();
				}
				try{
					AudioInputStream ris=new AudioInputStream(synthesis.getInputStream(),
							new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,24000,16,1,2,24000,false),
							AudioSystem.NOT_SPECIFIED);
					AudioInputStream ais=AudioSystem.getAudioInputStream(INPUT_FORMAT,ris);
					ais.skip(data_buffer.length);//20msにノイズが入ってる傾向
					ais.transferTo(buffer.getOutputStream());
				}catch(IOException e){
					e.printStackTrace();
				}
			}finally {
				query.close();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
