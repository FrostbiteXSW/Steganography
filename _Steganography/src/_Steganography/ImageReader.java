package _Steganography;

import java.io.File;
import java.io.IOException;

public abstract class ImageReader {
	protected File srcFile=null,	// 原始文件
				   srcIMG=null;		// 原始图片
	protected long size=0;			// 上次操作成功覆写的实际大小（字节）
	protected int level=-1;			// 隐写位数深度等级（1-3），默认为1
	protected Mode mode=null;			// 0表示隐写模式，1表示解析模式
	
	// 模式定义
	enum Mode {
		Steganography,	// 隐写模式
		UnSteganography	// 解析模式
	}
	
	// 类型定义
	enum Type {
		BMP,
		PNG
	}
	
	// 更改源图片
	public void setSrcIMG(String srcIMG) throws IOException {
		this.srcIMG=new File(srcIMG);
	}
	
	// 更改源文件
	public void setSrcFile(String srcFile) throws IOException {
		this.srcFile=new File(srcFile);
	}
	
	// 32位byte转int
    protected int byte2int(byte[] b) throws IOException {
        int num=(b[3]&0xff<<24)|(b[2]&0xff)<<16|(b[1]&0xff)<<8|(b[0]&0xff);
        return num;
    }
    
    // 任意长度byte数组转为int数组表示的bit流
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
    
    // 任意长度int数组表示的bit流转换为byte数组
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
    
    // 64位long转byte
    protected byte[] long2byte(long num) {
        byte[] byteNum = new byte[8];  
        for (int ix = 0; ix < 8; ++ix) {  
            int offset = 64 - (ix + 1) * 8;  
            byteNum[ix] = (byte) ((num >> offset) & 0xff);  
        }  
        return byteNum;  
    }
    
    // 64位byte转long
    protected long byte2long(byte[] bytenum) {  
        long num = 0;  
        for (int ix = 0; ix < 8; ++ix) {  
            num <<= 8;  
            num |= (bytenum[ix] & 0xff);  
        }
        return num; 
    }
    
    // 更改隐写深度，返回原始深度
 	public int setLevel(int level) throws Exception {
 		int temp=this.level;
 		if (level<1||level>3)
 			throw new Exception("深度等级越界。");
 		this.level=level;
 		return temp;
 	}
 	
 	// 更改模式，返回原始模式
 	public Mode setMode(Mode mode) throws Exception {
 		if (mode == null)
			throw new Exception("模式指定错误。");
 		Mode temp=this.mode;
 		this.mode=mode;
 		return temp;
 	}
 	
 	// 以当前设置执行隐写术或隐写术解析
 	public void exec(String outPut) throws Exception {
 		if (level<1 || level>3)
			throw new Exception("深度等级越界。");
 		switch (mode) {
 		case Steganography:
 			steganography(outPut);
 			break;
 		case UnSteganography:
 			unSteganography(outPut);
 			break;
 		}
 	}
 	
 	// 抽象隐写术方法
 	protected abstract void steganography(String outPut) throws IOException;
 	
 	// 抽象解析隐写术文件方法
 	protected abstract void unSteganography(String outPut) throws IOException;
 	
 	// 显示帮助信息
 	private static void displayHelpMessage() {
 		System.out.println(
 				"\n此工具可以对BMP和PNG图片进行隐写操作。"
 			  + "\n帮助信息如下（不区分大小写）：\n"
 			  + "\n参数设置："
 			  + "\n-s              隐写模式"
 			  + "\n-us             隐写解析模式"
 			  + "\n-p              以PNG格式处理"
 			  + "\n-b              以BMP格式处理"
 			  + "\n-l <1-3>        隐写等级"
 			  + "\n-i <filepath>   源图片"
 			  + "\n-f <filepath>   源文件"
 			  + "\n-o <filepath>   输出文件"
 			  + "\n-h,--help       显示此帮助信息\n"
 			  + "\n示例："
 			  + "\nImageReader -b -s -l 1 -i test.bmp -f test.txt -o out.bmp"
 			  + "\nImageReader -b -us -l 1 -i test.bmp -o out.txt");
 	}
 	
 	// 命令行外壳入口点
 	public static void main(String args[]) throws Exception {
 		if (args.length == 0) {
			System.out.println("\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\n"
							 + "////////////////////////////////////////////////////////////////////////////////\n"
							 + "                                                                                \n"
							 + "                          >>>>欢迎使用图片文件隐写工具<<<<                          \n"
							 + "                                                      作者：15124459 熊偲伟                 \n"
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
				System.out.println("未知的参数："+args[i]);
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
			throw new Exception("未指明的图片类型。");
		}
 		iReader.setSrcFile(srcFile);
 		iReader.setSrcIMG(srcIMG);
 		iReader.exec(outPut);
 		return;
 	}
}
