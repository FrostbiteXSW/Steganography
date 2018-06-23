package _BitIOStream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BitOutputStream {
	BufferedOutputStream bos;	// �����
	private byte byte_cur=0;	// ��ǰ�����е��ֽ�
	private int pos_next=0;		// ��һ�������λ��
	
	// ���캯����ʹ��File��
	public BitOutputStream(String fileName) throws IOException {
		bos=new BufferedOutputStream(new FileOutputStream(fileName));
	}
	
	// ���캯����ʹ��File��
	public BitOutputStream(File file) throws IOException {
		bos=new BufferedOutputStream(new FileOutputStream(file));
	}

	// ���캯����ʹ��OutputStream��
	public BitOutputStream(OutputStream OutputStream) {
		bos=new BufferedOutputStream(OutputStream);
	}
	
	// ������д��һλ���ݣ���bufС��0����Ϊ0������buf����1����Ϊ1����
	public void write(int buf) throws IOException {
		if (buf<=0) {
			byte_cur<<=1;
		} else {
			byte_cur=(byte) ((byte_cur<<1)|0x01);
		}
		if (++pos_next==8) {
			bos.write(byte_cur);
			pos_next=0;
			byte_cur=0;
		}
	}
	
	// ������д��һ������
	public void write(int[] buf) throws IOException {
		for (int i = 0; i < buf.length; i++) {
			write(buf[i]);
		}
	}
	
	// 	����ʼƫ�������Ŀռ��ڶ�ȡָ�����ȵ����ݲ�д������
	public void write(int[] buf,int off,int len) throws IOException {
		if (len<0||off<0||off+len>buf.length) {
			throw new ArrayIndexOutOfBoundsException();
		}
		for (int i = off; i < len+off; i++) {
			write(buf[i]);
		}
	}

	// ����ˢ������������д�����ݣ������Թ���һ���ֽڵ������Զ���0��
	public void flush() throws IOException {
		if (pos_next==0) {
			bos.flush();
		} else {
			for (int i = 0; i < 8 - pos_next; i++) {
				byte_cur<<=1;
			}
			bos.write(byte_cur);
			bos.flush();
			pos_next=0;
			byte_cur=0;
		}
	}
	
	// �ر���
	public void close() throws IOException {
		flush();
		bos.close();
		pos_next=-1;
		byte_cur=0;
	}
}
