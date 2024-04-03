package xyz.kzkr.vvtalk;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

public class ByteArrayIOBuffer{
	private final ByteBuffer buf;
	private int rpos;//読み込み基点
	private int wpos;//書き込み基点
	private int size;//有効データ数
	private boolean eof;//書き込み終了
	private Object lock=new Object();
	private Object wwait=new Object();//書き待ち
	private Object rwait=new Object();//読み待ち
	private Input is;
	private Output os;
	public ByteArrayIOBuffer(byte[] b){
		this(ByteBuffer.wrap(b));
	}
	public ByteArrayIOBuffer(ByteBuffer b){
		buf=Objects.requireNonNull(b);
		if(buf.capacity()<1) throw new java.lang.IllegalArgumentException("buffer length < 1");
		if(buf.isReadOnly()) throw new java.lang.IllegalArgumentException("buffer ReadOnly");
		is=new Input();
		os=new Output();
	}
	public InputStream getInputStream() {
		return is;
	}
	public OutputStream getOutputStream() {
		return os;
	}
	private class Input extends InputStream{
		byte[] b1=new byte[1];
		@Override
		public synchronized int read() throws IOException{
			read(b1,0,1);
			if(b1[0]==-1)return -1;
			return b1[0]&0xFF;
		}
		@Override
		public synchronized int read(byte b[],int off,int len) throws IOException{
			Objects.checkFromIndexSize(off,len,b.length);
			if(len==0)return 0;
			int readCount=0;
			while(len>0){
				if(eof&&size<1) break;
				if(readCount>0&&size<1)return readCount;
				int r=0;
				synchronized(lock){
					if(size>0) {
						r+=readSub(b,off,Math.min(size,len));
						synchronized(rwait) {
							rwait.notify();
						}
					}
				}
				synchronized(wwait) {
					if(r<1){
						try{
							wwait.wait();
						}catch(InterruptedException e){
							throw new InterruptedIOException();
						}
						continue;
					}
				}
				len-=r;
				off+=r;
				readCount+=r;
			}
			if(readCount==0&&eof)return -1;
			return readCount;
		}
		private int readSub(byte[] b,int off,int len) {
			int pos=rpos+len;
			if(pos>=buf.capacity())pos=buf.capacity();
			int s=pos-rpos;
			buf.position(rpos);
			buf.get(b,off,s);
			rpos+=s;
			if(rpos>=buf.capacity()) {
				rpos=0;
			}
			size-=s;
			return s;
		}
		@Override
		public synchronized long skip(long len) throws IOException {
			if(len<0)return 0;
			len=Math.min(size,len);
			int pos=(int) (Math.min(len+rpos,Integer.MAX_VALUE));
			if(pos<0)return 0;
			if(pos>=buf.capacity())pos=buf.capacity();
			int skip=pos-rpos;
			if(skip>0) {
				rpos+=skip;
				if(rpos>=buf.capacity()) {
					rpos=0;
				}
				size-=skip;
				synchronized(rwait) {
					rwait.notify();
				}
			}
			return skip;
		}
		@Override
		public int available() {
			return size;
		}
	}
	private class Output extends OutputStream{
		byte[] b1=new byte[1];
		@Override
		public synchronized void write(int b) throws IOException{
			b1[0]=(byte) b;
			write(b1,0,1);
		}
		@Override
		public synchronized void write(byte b[],int off,int len) throws IOException{
			Objects.checkFromIndexSize(off,len,b.length);
			while(len>0){
				if(eof) throw new IOException("Stream Closed");
				int w=0,bufferEmpty;
				synchronized(lock){
					bufferEmpty=buf.capacity()-size;//容量-使用済=空き
					//空き容量か書き込み希望の小さい方を書き込んで
					if(bufferEmpty>0) {
						w=writeSub(b,off,Math.min(bufferEmpty,len));
						if(w>0)synchronized(wwait) {
							wwait.notify();
						}
					}
				}
				synchronized(rwait) {
					if(bufferEmpty<1){
						try{
							rwait.wait();
						}catch(InterruptedException e){
							throw new InterruptedIOException();
						}
						continue;
					}
				}
				//書き込めた量だけ書き込みたい量から減らす
				len-=w;
				off+=w;
			}
		}
		private int writeSub(byte[] b,int off,int len){
			int alen=wpos+len;
			if(alen>=buf.capacity())alen=buf.capacity();//超過後半
			int sw=alen-wpos;
			buf.position(wpos);
			buf.put(b,off,sw);
			size+=sw;
			wpos+=sw;
			if(wpos>=buf.capacity()) {
				wpos=0;
			}
			return sw;
		}
		@Override
		public void close(){
			eof=true;
			synchronized(rwait) {
				rwait.notify();
			}
			synchronized(wwait) {
				wwait.notify();
			}
		}
	}
	public boolean isClosed() {
		return eof;
	}
}
