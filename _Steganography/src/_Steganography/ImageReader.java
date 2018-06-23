package _Steganography;

import java.io.File;
import java.io.IOException;

public abstract class ImageReader {
	protected File srcFile=null,	// ԭʼ�ļ�
				   srcIMG=null;		// ԭʼͼƬ
	protected long size=0;			// �ϴβ����ɹ���д��ʵ�ʴ�С���ֽڣ�
	protected int level=-1;			// ��дλ����ȵȼ���1-3����Ĭ��Ϊ1
	protected Mode mode=null;			// 0��ʾ��дģʽ��1��ʾ����ģʽ
	
	// ģʽ����
	enum Mode {
		Steganography,	// ��дģʽ
		UnSteganography	// ����ģʽ
	}
	
	// ���Ͷ���
	enum Type {
		BMP,
		PNG
	}
	
	// ����ԴͼƬ
	public void setSrcIMG(String srcIMG) throws IOException {
		this.srcIMG=new File(srcIMG);
	}
	
	// ����Դ�ļ�
	public void setSrcFile(String srcFile) throws IOException {
		this.srcFile=new File(srcFile);
	}
	
	// 32λbyteתint
    protected int byte2int(byte[] b) throws IOException {
        int num=(b[3]&0xff<<24)|(b[2]&0xff)<<16|(b[1]&0xff)<<8|(b[0]&0xff);
        return num;
    }
    
    // ���ⳤ��byte����תΪint�����ʾ��bit��
    protected int[] byte2bit(byte[] b) {
		byte[] bytearray=b.clone();
		int[] bitstream=new int[bytearray.length*8];
		for (int i = 0; i < bytearray.length; i++) {
			for (int j = 0; j < 8; j++) {
				bitstream[i*8+j]=(bytearray[i]>>7)&0x01;
				bytearray[i]<<=1;
			}
		}
		return bitstream;
	}
    
    // ���ⳤ��int�����ʾ��bit��ת��Ϊbyte����
    protected byte[] bit2byte(int[] b) {
		int[] bitstream=b.clone();
		byte[] bytearray=new byte[(bitstream.length/8)+(bitstream.length%8==0?0:1)];
		for (int i = 0; i < bytearray.length; i++) {
			for (int j = 0; j < 8; j++) {
				if (i*8+j>=bitstream.length)
					bytearray[i]<<=1;
				else
					bytearray[i]=(byte) (bitstream[i*8+j]==0?(bytearray[i]<<1):((bytearray[i]<<1)|0x01));
			}
		}
		return bytearray;
	}
    
    // 64λlongתbyte
    protected byte[] long2byte(long num) {
        byte[] byteNum = new byte[8];  
        for (int ix = 0; ix < 8; ++ix) {  
            int offset = 64 - (ix + 1) * 8;  
            byteNum[ix] = (byte) ((num >> offset) & 0xff);  
        }  
        return byteNum;  
    }
    
    // 64λbyteתlong
    protected long byte2long(byte[] bytenum) {  
        long num = 0;  
        for (int ix = 0; ix < 8; ++ix) {  
            num <<= 8;  
            num |= (bytenum[ix] & 0xff);  
        }
        return num; 
    }
    
    // ������д��ȣ�����ԭʼ���
 	public int setLevel(int level) throws Exception {
 		int temp=this.level;
 		if (level<1||level>3)
 			throw new Exception("��ȵȼ�Խ�硣");
 		this.level=level;
 		return temp;
 	}
 	
 	// ����ģʽ������ԭʼģʽ
 	public Mode setMode(Mode mode) throws Exception {
 		if (mode == null)
			throw new Exception("ģʽָ������");
 		Mode temp=this.mode;
 		this.mode=mode;
 		return temp;
 	}
 	
 	// �Ե�ǰ����ִ����д������д������
 	public void exec(String outPut) throws Exception {
 		if (level<1 || level>3)
			throw new Exception("��ȵȼ�Խ�硣");
 		switch (mode) {
 		case Steganography:
 			steganography(outPut);
 			break;
 		case UnSteganography:
 			unSteganography(outPut);
 			break;
 		}
 	}
 	
 	// ������д������
 	protected abstract void steganography(String outPut) throws IOException;
 	
 	// ���������д���ļ�����
 	protected abstract void unSteganography(String outPut) throws IOException;
 	
 	// ��ʾ������Ϣ
 	private static void displayHelpMessage() {
 		System.out.println(
 				"\n�˹��߿��Զ�BMP��PNGͼƬ������д������"
 			  + "\n������Ϣ���£������ִ�Сд����\n"
 			  + "\n�������ã�"
 			  + "\n-s              ��дģʽ"
 			  + "\n-us             ��д����ģʽ"
 			  + "\n-p              ��PNG��ʽ����"
 			  + "\n-b              ��BMP��ʽ����"
 			  + "\n-l <1-3>        ��д�ȼ�"
 			  + "\n-i <filepath>   ԴͼƬ"
 			  + "\n-f <filepath>   Դ�ļ�"
 			  + "\n-o <filepath>   ����ļ�"
 			  + "\n-h,--help       ��ʾ�˰�����Ϣ\n"
 			  + "\nʾ����"
 			  + "\nImageReader -b -s -l 1 -i test.bmp -f test.txt -o out.bmp"
 			  + "\nImageReader -b -us -l 1 -i test.bmp -o out.txt");
 	}
 	
 	// �����������ڵ�
 	public static void main(String args[]) throws Exception {
 		if (args.length == 0) {
			System.out.println("\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\n"
							 + "////////////////////////////////////////////////////////////////////////////////\n"
							 + "                                                                                \n"
							 + "                          >>>>��ӭʹ��ͼƬ�ļ���д����<<<<                          \n"
							 + "                                                      ���ߣ�15124459 �܂�ΰ                 \n"
							 + "                                                                                \n"
							 + "////////////////////////////////////////////////////////////////////////////////\n"
							 + "\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\n");
			return;
		}
 		int level=-1;
 		Mode mode=null;
 		Type type=null;
 		String srcFile="",srcIMG="",outPut="";
 		for (int i = 0; i < args.length; i++) {
			switch (args[i].toLowerCase()) {
			case "-h":
				displayHelpMessage();
				return;
			case "--help":
				displayHelpMessage();
				return;
			case "-s":
				mode=Mode.Steganography;
				break;
			case "-us":
				mode=Mode.UnSteganography;
				break;
			case "-p":
				type=Type.PNG;
				break;
			case "-b":
				type=Type.BMP;
				break;
			case "-l":
				level=Integer.parseInt(args[++i]);
				break;
			case "-i":
				srcIMG=args[++i];
				break;
			case "-f":
				srcFile=args[++i];
				break;
			case "-o":
				outPut=args[++i];
				break;
			default:
				System.out.println("δ֪�Ĳ�����"+args[i]);
				return;
			}
		}
 		ImageReader iReader=null;
 		switch (type) {
		case BMP:
			iReader=new BMPReader(level, mode);
			break;
		case PNG:
			iReader=new PNGReader(level, mode);
			break;
		default:
			throw new Exception("δָ����ͼƬ���͡�");
		}
 		iReader.setSrcFile(srcFile);
 		iReader.setSrcIMG(srcIMG);
 		iReader.exec(outPut);
 		return;
 	}
}
