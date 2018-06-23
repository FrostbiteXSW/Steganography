package _Steganography;

import java.io.*;

public class BMPReader extends ImageReader {
	// 带参构造函数
	public BMPReader(int level,Mode mode) throws Exception {
		setLevel(level);
		setMode(mode);
	}
	
	// 读取图片像素信息，返回三维数组（行号，列号，RGB）
	private int[][][] readBMP(File src) throws IOException {
        FileInputStream fisBMP=new FileInputStream(src);			// 原始图片流
        BufferedInputStream bisBMP=new BufferedInputStream(fisBMP);

        // 丢掉文件头信息
        bisBMP.skip(18);

        // 获取长度与宽度
        byte[] b1=new byte[4];
        bisBMP.read(b1);
        byte[] b2=new byte[4];
        bisBMP.read(b2);

        int Width=byte2int(b1);
        int Height=byte2int(b2);

        // 因为BMP位图的读取顺序为横向扫描，所以应把数组定义为int[Height][Width]
        int[][][] data=new int[Height][Width][3];
        int skipnum=0;

        // BMP图像区域的大小必须为4的倍数，而它以三个字节存一个像素，读的时候应当跳过补上的0
        if(Width*3%4!=0)
            skipnum=4-Width*3%4;
        
        bisBMP.skip(28);
        
        for(int i=0;i<data.length;i++) {
            for(int j=0;j<data[i].length;j++) {
                data[i][j][0]=bisBMP.read();		//R
                data[i][j][1]=bisBMP.read();		//G
                data[i][j][2]=bisBMP.read();		//B
            }
            if(skipnum!=0)
                bisBMP.skip(skipnum);
        }
        
        bisBMP.close();
        return data;
    }
	
	// 解析隐写术文件（文件长定界），导出至指定文件路径
	protected void unSteganography(String outPut) throws IOException {
		int[][][] data=readBMP(srcIMG);	// 获取图片像素信息
		
		// 清空size值
        size=0;
        
		// 创建输出文件
        File file=new File(outPut);
        if (file.exists()) {
        	if (file.isFile())
				file.delete();
        }
        file.createNewFile();
        
        // 创建输出文件流
        BufferedOutputStream bosFile=new BufferedOutputStream(new FileOutputStream(file));	// 输出图片流
		int ret=-1;		// 保存操作返回值
		
		// 根据深度等级选择算法
		switch (level) {
		case 1:
			ret=unSteganography_L1(data, bosFile);
			break;
		case 2:
			ret=unSteganography_L2(data, bosFile);
			break;
		case 3:
			ret=unSteganography_L3(data, bosFile);
			break;
		}
		
		// 未能正确解析文件
		if (ret==-1) {
			System.out.println("解析文件失败。");
			bosFile.close();
			file.delete();
			return;
		}
		
		// 解析文件完成
		System.out.println("解析文件完成，总大小：   "+size+" Byte");
		bosFile.close();
		return;
	}
	
	// 解析隐写术文件（文件长定界，深度等级1），成功返回0，失败返回-1
	private int unSteganography_L1(int[][][] data,BufferedOutputStream bosFile) throws IOException {
		// 读取图片像素数据
		byte buf=0,			// 存放当前字节数据
			 temp=0;		// 存放data转换为byte的临时数据
        int count=0,		// 存放当前字节剩余需要写入位数
        	times=8;		// 存放长度字节剩余读取位数
        byte[] lenthbuf=new byte[8];
        					// 长度字节缓存
        long lenth=0;		// 文件长度
        boolean EOL=false;	// 读取文件长度结束标志
        
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				for (int k = 0; k < 3; k++) {
					temp=(byte)((data[i][j][k]&0xFF)&0x01);
					
					if (!EOL) { // 读取文件长度
						if (times!=0) {
							lenthbuf[times-1]=(byte)((lenthbuf[times-1]>>1)&0x7F);
							lenthbuf[times-1]|=temp<<7;
							if (++count==8) {
								times--;
								count=0;
							}
						} else {
							lenth=byte2long(lenthbuf);
							buf=(byte)((buf>>1)&0x7F);
							buf|=temp<<7;
							count++;
							EOL=true;
						}
					} else if (lenth>0) { // 根据剩余长度读取相应数量的字节
						buf=(byte)((buf>>1)&0x7F);
						buf|=temp<<7;
						if (++count==8) {
							bosFile.write(buf);
							size++;
							lenth--;
							count=0;
						}
					} else // 读取完成
						return 0;
				}
			}
		}
		
		// 未能正确解析文件
		return -1;
	}
	
	// 解析隐写术文件（文件长定界，深度等级2），成功返回0，失败返回-1
	private int unSteganography_L2(int[][][] data,BufferedOutputStream bosFile) throws IOException {
		// 读取图片像素数据
		byte buf=0,			// 存放当前字节数据
			 temp=0;		// 存放data转换为byte的临时数据
        int count=0,		// 存放当前字节剩余需要写入位数
        	times=8;		// 存放长度字节剩余读取位数
        byte[] lenthbuf=new byte[8];
        					// 长度字节缓存
        long lenth=0;		// 文件长度
        boolean EOL=false;	// 读取文件长度结束标志
        
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				for (int k = 0; k < 3; k++) {
					temp=(byte)((data[i][j][k]&0xFF)&0x03);
					
					if (!EOL) { // 读取文件长度
						if (times!=0) {
							lenthbuf[times-1]=(byte)((lenthbuf[times-1]>>2)&0x3F);
							lenthbuf[times-1]|=temp<<6;
							if (++count==4) {
								times--;
								count=0;
							}
						} else {
							lenth=byte2long(lenthbuf);
							buf=(byte)((buf>>2)&0x3F);
							buf|=temp<<6;
							count++;
							EOL=true;
						}
					} else if (lenth>0) { // 根据剩余长度读取相应数量的字节
						buf=(byte)((buf>>2)&0x3F);
						buf|=temp<<6;
						if (++count==4) {
							bosFile.write(buf);
							size++;
							lenth--;
							count=0;
						}
					} else // 读取完成
						return 0;
				}
			}
		}
		
		// 未能正确解析文件
		return -1;
	}
	
	// 解析隐写术文件（文件长定界，深度等级3），成功返回0，失败返回-1
	private int unSteganography_L3(int[][][] data,BufferedOutputStream bosFile) throws IOException {
		// 读取图片像素数据
		byte buf=0,			// 存放当前字节数据
			 temp=0;		// 存放data转换为byte的临时数据
        int count=0,		// 存放当前字节剩余需要写入位数
        	times=8;		// 存放长度字节剩余读取位数
        byte[] lenthbuf=new byte[8];
        					// 长度字节缓存
        long lenth=0;		// 文件长度
        boolean EOL=false;	// 读取文件长度结束标志
        
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				for (int k = 0; k < 3; k++) {
					temp=(byte)((data[i][j][k]&0xFF)&0x0F);
					
					if (!EOL) { // 读取文件长度
						if (times!=0) {
							lenthbuf[times-1]=(byte)((lenthbuf[times-1]>>4)&0x0F);
							lenthbuf[times-1]|=temp<<4;
							if (++count==2) {
								times--;
								count=0;
							}
						} else {
							lenth=byte2long(lenthbuf);
							buf=(byte)((buf>>4)&0x0F);
							buf|=temp<<4;
							count++;
							EOL=true;
						}
					} else if (lenth>0) { // 根据剩余长度读取相应数量的字节
						buf=(byte)((buf>>4)&0x0F);
						buf|=temp<<4;
						if (++count==2) {
							bosFile.write(buf);
							size++;
							lenth--;
							count=0;
						}
					} else // 读取完成
						return 0;
				}
			}
		}
		
		// 未能正确解析文件
		return -1;
	}
	
	// 隐写术（文件长定界），将指定文件写入指定图片，并保存为新图片文件
	protected void steganography(String outPut) throws IOException {
		BufferedInputStream bisBMP=new BufferedInputStream(new FileInputStream(srcIMG)),	// 原始图片流
							bisFile=new BufferedInputStream(new FileInputStream(srcFile));	// 原始文件流

		// 清空size值
		size=0;
		
		// 创建输出文件
		File file=new File(outPut);
		if (file.exists()) {
			if (file.isFile())
				file.delete();
		}
		file.createNewFile();
		
		BufferedOutputStream bosBMP=new BufferedOutputStream(new FileOutputStream(file));	// 输出图片流
		int ret=-1;		// 保存操作返回值
		
		switch (level) {
			case 1:
				ret=steganography_L1(bisBMP, bisFile, bosBMP);
				break;
			case 2:
				ret=steganography_L2(bisBMP, bisFile, bosBMP);
				break;
			case 3:
				ret=steganography_L3(bisBMP, bisFile, bosBMP);
				break;
		}
		
		// 方法完成，关闭所有流
		bisBMP.close();
		bisFile.close();
		bosBMP.close();
		
		// 判断是否成功写入
		if (ret==-1) {
			System.out.println("图片大小不足，写入失败。");
			size=0;
			file.delete();
		}
		else
			System.out.println("写入完成，总覆写大小：   "+(size/=8)+" Byte");
	}

	// 隐写术（文件长定界，深度等级1），成功返回0，失败返回-1
	private int steganography_L1(BufferedInputStream bisBMP,BufferedInputStream bisFile,BufferedOutputStream bosBMP) throws IOException {
		// 拷贝文件头信息
        for (int i = 0; i < 18; i++)
			bosBMP.write(bisBMP.read());
        
        // 获取并拷贝长度与宽度
        byte[] b1=new byte[4];
        bisBMP.read(b1);
        bosBMP.write(b1);
        byte[] b2=new byte[4];
        bisBMP.read(b2);
        bosBMP.write(b2);

        int Width=byte2int(b1);
        int Height=byte2int(b2);
        
        // 图片大小不足，不能写完
        if (((long)Width*(long)Height*3)<(srcFile.length()*8+64))
			return -1;
        
        // 计算跳过的0的位数
        int skipnum=0;
        if(Width*3%4!=0)
            skipnum=4-Width*3%4;
        
        // 拷贝文件信息
        for (int i = 0; i < 28; i++)
			bosBMP.write(bisBMP.read());
        
        // 拷贝并隐写文件
        byte buf=0;			// 存放当前字节数据
        int count=0;		// 存放当前字节剩余需要写入位数
        byte[] lenth=long2byte(srcFile.length());	// 存放写入的长度信息
        int times=8;		// 存放lenth剩余写入次数信息
        boolean EOF=false,	// 判断是否读到文件尾部
        		EOL=false;	// 判断长度信息是否写完
        
        for(int i=0;i<Height;i++) { // 文件总大小=行*列*RGB，此为循环总次数，亦为能够隐写的文件总位数
        	for(int j=0;j<Width*3;j++) {
        		// 判断长度信息是否写完
        		if (!EOL) {
        			// 判断上一次读入的8位数据是否已隐写完毕，如果写完则再从lenth中读取8位数据
            		if(count==0) {
                		// 判断是否读到lenth尾部
                		if(times==0) {
                			EOL=true;
                			
                			// 从文件流中读数据
                			int temp=bisFile.read();	// 读取文件流的下一个字节
                    		
                    		// 判断是否读到文件尾部
                    		if(temp==-1) {
                    			EOF=true;
                    			bosBMP.write(bisBMP.read());
    	    	        		continue;
                    		}
                    		count=8;	// 已读取文件流中的后8位数据
                    		buf=(byte)(temp&0xFF);	// 保存为无符号数
                		}
                		else {
							buf=lenth[--times];
							count=8;
						}
                	}
            		
            		// 取出buf中数据的低位，替换原始图片流下个字节的低位，然后写入输出图片流中，同时buf右移清除最低位数据
	        		bosBMP.write((bisBMP.read()&0xFE)|(buf&0x01));
	        		size++;buf>>>=1;count--;
				}
        		// 判断文件是否已写完，未写完则继续隐写
        		else if (!EOF) {
        			// 判断上一次读入的8位数据是否已隐写完毕，如果写完则再从文件流中读取8位数据
            		if(count==0) {
                		int temp=bisFile.read();	// 读取文件流的下一个字节
                		
                		// 判断是否读到文件尾部
                		if(temp==-1) {
                			EOF=true;
                			bosBMP.write(bisBMP.read());
	    	        		continue;
                		}
                		count=8;	// 已读取文件流中的后8位数据
                		buf=(byte)(temp&0xFF);	// 保存为无符号数
                	}
            		
            		// 取出buf中数据的低位，替换原始图片流下个字节的低位，然后写入输出图片流中，同时buf右移清除最低位数据
	        		bosBMP.write((bisBMP.read()&0xFE)|(buf&0x01));
	        		size++;buf>>>=1;count--;
	        	}
	    		// 写完则跳过隐写直接拷贝
	        	else
	        		bosBMP.write(bisBMP.read());
        	}
    		// 直接拷贝跳过的0信息
            if(skipnum!=0) {
            	for (int j = 0; j < skipnum; j++)
        			bosBMP.write(bisBMP.read());
            }
        }
        
        return 0;
	}

	// 隐写术（文件长定界，深度等级2），成功返回0，失败返回-1
	private int steganography_L2(BufferedInputStream bisBMP,BufferedInputStream bisFile,BufferedOutputStream bosBMP) throws IOException {
		// 拷贝文件头信息
        for (int i = 0; i < 18; i++)
			bosBMP.write(bisBMP.read());
        
        // 获取并拷贝长度与宽度
        byte[] b1=new byte[4];
        bisBMP.read(b1);
        bosBMP.write(b1);
        byte[] b2=new byte[4];
        bisBMP.read(b2);
        bosBMP.write(b2);

        int Width=byte2int(b1);
        int Height=byte2int(b2);
        
        // 图片大小不足，不能写完
        if (((long)Width*(long)Height*3)<(srcFile.length()*4+32))
			return -1;
        
        // 计算跳过的0的位数
        int skipnum=0;
        if(Width*3%4!=0)
            skipnum=4-Width*3%4;
        
        // 拷贝文件信息
        for (int i = 0; i < 28; i++)
			bosBMP.write(bisBMP.read());
        
        // 拷贝并隐写文件
        byte buf=0;			// 存放当前字节数据
        int count=0;		// 存放当前字节剩余需要写入位数
        byte[] lenth=long2byte(srcFile.length());	// 存放写入的长度信息
        int times=8;		// 存放lenth剩余写入次数信息
        boolean EOF=false,	// 判断是否读到文件尾部
        		EOL=false;	// 判断长度信息是否写完
        
        for(int i=0;i<Height;i++) { // 文件总大小=行*列*RGB，此为循环总次数，亦为能够隐写的文件总位数
        	for(int j=0;j<Width*3;j++) {
        		// 判断长度信息是否写完
        		if (!EOL) {
        			// 判断上一次读入的8位数据是否已隐写完毕，如果写完则再从lenth中读取8位数据
            		if(count==0) {
                		// 判断是否读到lenth尾部
                		if(times==0) {
                			EOL=true;
                			
                			// 从文件流中读数据
                			int temp=bisFile.read();	// 读取文件流的下一个字节
                    		
                    		// 判断是否读到文件尾部
                    		if(temp==-1) {
                    			EOF=true;
                    			bosBMP.write(bisBMP.read());
    	    	        		continue;
                    		}
                    		count=8;	// 已读取文件流中的后8位数据
                    		buf=(byte)(temp&0xFF);	// 保存为无符号数
                		}
                		else {
							buf=lenth[--times];
							count=8;
						}
                	}
            		
            		// 取出buf中数据的低位，替换原始图片流下个字节的低位，然后写入输出图片流中，同时buf右移清除最低位数据
	        		bosBMP.write((bisBMP.read()&0xFC)|(buf&0x03));
	        		size+=2;buf>>>=2;count-=2;
				}
        		// 判断文件是否已写完，未写完则继续隐写
        		else if (!EOF) {
        			// 判断上一次读入的8位数据是否已隐写完毕，如果写完则再从文件流中读取8位数据
            		if(count==0) {
                		int temp=bisFile.read();	// 读取文件流的下一个字节
                		
                		// 判断是否读到文件尾部
                		if(temp==-1) {
                			EOF=true;
                			bosBMP.write(bisBMP.read());
	    	        		continue;
                		}
                		count=8;	// 已读取文件流中的后8位数据
                		buf=(byte)(temp&0xFF);	// 保存为无符号数
                	}
            		
            		// 取出buf中数据的低位，替换原始图片流下个字节的低位，然后写入输出图片流中，同时buf右移清除最低位数据
	        		bosBMP.write((bisBMP.read()&0xFC)|(buf&0x03));
	        		size+=2;buf>>>=2;count-=2;
	        	}
	    		// 写完则跳过隐写直接拷贝
	        	else
	        		bosBMP.write(bisBMP.read());
        	}
    		// 直接拷贝跳过的0信息
            if(skipnum!=0) {
            	for (int j = 0; j < skipnum; j++)
        			bosBMP.write(bisBMP.read());
            }
        }
        
        return 0;
	}

	// 隐写术（文件长定界，深度等级3），成功返回0，失败返回-1
	private int steganography_L3(BufferedInputStream bisBMP,BufferedInputStream bisFile,BufferedOutputStream bosBMP) throws IOException {
		// 拷贝文件头信息
        for (int i = 0; i < 18; i++)
			bosBMP.write(bisBMP.read());
        
        // 获取并拷贝长度与宽度
        byte[] b1=new byte[4];
        bisBMP.read(b1);
        bosBMP.write(b1);
        byte[] b2=new byte[4];
        bisBMP.read(b2);
        bosBMP.write(b2);

        int Width=byte2int(b1);
        int Height=byte2int(b2);
        
        // 图片大小不足，不能写完
        if (((long)Width*(long)Height*3)<(srcFile.length()*2+16))
			return -1;
		
        // 计算跳过的0的位数
        int skipnum=0;
        if(Width*3%4!=0)
            skipnum=4-Width*3%4;
        
        // 拷贝文件信息
        for (int i = 0; i < 28; i++)
			bosBMP.write(bisBMP.read());
        
        // 拷贝并隐写文件
        byte buf=0;			// 存放当前字节数据
        int count=0;		// 存放当前字节剩余需要写入位数
        byte[] lenth=long2byte(srcFile.length());	// 存放写入的长度信息
        int times=8;		// 存放lenth剩余写入次数信息
        boolean EOF=false,	// 判断是否读到文件尾部
        		EOL=false;	// 判断长度信息是否写完
        
        for(int i=0;i<Height;i++) { // 文件总大小=行*列*RGB，此为循环总次数，亦为能够隐写的文件总位数
        	for(int j=0;j<Width*3;j++) {
        		// 判断长度信息是否写完
        		if (!EOL) {
        			// 判断上一次读入的8位数据是否已隐写完毕，如果写完则再从lenth中读取8位数据
            		if(count==0) {
                		// 判断是否读到lenth尾部
                		if(times==0) {
                			EOL=true;
                			
                			// 从文件流中读数据
                			int temp=bisFile.read();	// 读取文件流的下一个字节
                    		
                    		// 判断是否读到文件尾部
                    		if(temp==-1) {
                    			EOF=true;
                    			bosBMP.write(bisBMP.read());
    	    	        		continue;
                    		}
                    		count=8;	// 已读取文件流中的后8位数据
                    		buf=(byte)(temp&0xFF);	// 保存为无符号数
                		}
                		else {
							buf=lenth[--times];
							count=8;
						}
                	}
            		
            		// 取出buf中数据的低位，替换原始图片流下个字节的低位，然后写入输出图片流中，同时buf右移清除最低位数据
	        		bosBMP.write((bisBMP.read()&0xF0)|(buf&0x0F));
	        		size+=4;buf>>>=4;count-=4;
				}
        		// 判断文件是否已写完，未写完则继续隐写
        		else if (!EOF) {
        			// 判断上一次读入的8位数据是否已隐写完毕，如果写完则再从文件流中读取8位数据
            		if(count==0) {
                		int temp=bisFile.read();	// 读取文件流的下一个字节
                		
                		// 判断是否读到文件尾部
                		if(temp==-1) {
                			EOF=true;
                			bosBMP.write(bisBMP.read());
	    	        		continue;
                		}
                		count=8;	// 已读取文件流中的后8位数据
                		buf=(byte)(temp&0xFF);	// 保存为无符号数
                	}
            		
            		// 取出buf中数据的低位，替换原始图片流下个字节的低位，然后写入输出图片流中，同时buf右移清除最低位数据
	        		bosBMP.write((bisBMP.read()&0xF0)|(buf&0x0F));
	        		size+=4;buf>>>=4;count-=4;
	        	}
	    		// 写完则跳过隐写直接拷贝
	        	else
	        		bosBMP.write(bisBMP.read());
        	}
    		// 直接拷贝跳过的0信息
            if(skipnum!=0) {
            	for (int j = 0; j < skipnum; j++)
        			bosBMP.write(bisBMP.read());
            }
        }
        
        return 0;
	}
}
