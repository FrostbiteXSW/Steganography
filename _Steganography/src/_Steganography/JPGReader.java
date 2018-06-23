package _Steganography;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import _BitIOStream.BitInputStream;
import _Steganography.JPGReader.HuffmanTable.HuffmanTableType;

public class JPGReader extends ImageReader {
	public static void main(String[] args) {
		JPGReader jpgReader;
		try {
			jpgReader = new JPGReader(1,Mode.Steganography);
			showHuffmanTableInfo("test.jpg");
			ArrayList<MCUMatrix> MCUMatrixs = jpgReader.decodeSOS("test.jpg");
			System.out.println("共解析出" + MCUMatrixs.size() + "张表。");
			System.in.read();
			int count = 0;
			for (MCUMatrix MCUMatrix : MCUMatrixs) {
				System.out.println("表编号" + (++count));
				System.out.println("表类型：" + MCUMatrix.type.toString());
				System.out.println("表内容如下：");
				for (int i = 0; i < 64; i++) {
					System.out.print(MCUMatrix.matrix[i] + " ");
					if (i % 8 == 7) {
						System.out.println();
					}
				}
				if (count % 20 == 0) {
					System.out.println("按任意键继续输出");
					System.in.read();
				}
			}
		} catch (Exception e) {
			System.out.print("Exception:");
			e.printStackTrace();
			File file = new File("tempfile.tmp");
			if (file.exists())
				file.delete();
		}
	}
	
	// 带参构造函数
	public JPGReader(int level,Mode mode) throws Exception {
		setLevel(level);
		setMode(mode);
	}
	
	private ERRCODE LAST_ERROR = ERRCODE.NO_ERROR;	// 上一次发生的错误类型
	
	// 错误码
	enum ERRCODE {
		NO_ERROR,		// 无错误
		FORMAT_ERROR,	// 格式错误
		RESOLVE_ERROR,	// 解析错误
	}
	
	// 获取上一次发生的错误类型
	public String getLastError() {
		return LAST_ERROR.toString();
	}
	
	// 哈夫曼树单个叶子节点
	class HuffmanTableLeafPoint {
		int[] code;				// 码字
		byte value = 0x00;		// 权值
	}
	
	// 哈夫曼表类
	class HuffmanTable {
		// 哈夫曼表类型
		class HuffmanTableType {
			static final byte DC0 = 0x00,	// DC直流0号表
							  DC1 = 0x01,	// DC直流1号表
							  AC0 = 0x10,	// AC交流0号表
							  AC1 = 0x11;	// AC交流1号表	  
		}
		
		byte type;
		int length;
		HuffmanTableLeafPoint[] points;
	}
	
	// 显示JPG文件哈夫曼表信息（测试用途）
	public static void showHuffmanTableInfo(String filePath) throws Exception {
		JPGReader jpgReader = new JPGReader(1, Mode.Steganography);
		ArrayList<HuffmanTable> huffmanTables = jpgReader.getHuffmanTable(filePath);
		System.out.println("操作的返回结果：" + jpgReader.getLastError());
		if (jpgReader.getLastError() != ERRCODE.NO_ERROR.toString())
			return;
		System.out.println("总共解析出的哈夫曼表数：" + huffmanTables.size());
		int tableIndex = 0;
		for (HuffmanTable huffmanTable : huffmanTables) {
			System.out.println("\n哈夫曼表：" + (++tableIndex));
			System.out.print("表类型：");
			switch (huffmanTable.type) {
			case HuffmanTableType.AC0:
				System.out.println("AC0");
				break;
			case HuffmanTableType.AC1:
				System.out.println("AC1");
				break;
			case HuffmanTableType.DC0:
				System.out.println("DC0");
				break;
			case HuffmanTableType.DC1:
				System.out.println("DC1");
				break;
			}
			System.out.println("表内容：");
			int leafIndex = 1;
			for (HuffmanTableLeafPoint huffmanTableLeafPoint : huffmanTable.points) {
				String codeStr = "";
				for (int code : huffmanTableLeafPoint.code) {
					switch (code) {
					case 0:
						codeStr += '0';
						break;
					case 1:
						codeStr += '1';
						break;
					}
				}
				System.out.printf("%3d. %16s  0x%02x\n", leafIndex++, codeStr, huffmanTableLeafPoint.value);
			}
		}
	}
	
	// 从JPG文件中解析哈夫曼表
	private ArrayList<HuffmanTable> getHuffmanTable(String filePath) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath));
		if (bis.read() != 0xFF || bis.read() != 0xD8) {
			LAST_ERROR = ERRCODE.FORMAT_ERROR;
			bis.close();
			return null;
		}
		ArrayList<HuffmanTable> huffmanTables = new ArrayList<>();	// 哈夫曼表集合
		while (bis.available() > 0) {
			if (bis.read() == 0xFF) {
				int temp = bis.read();
				if (temp == 0xC4) {
					// 获取DHT段长
					int DHTLength = ((bis.read() << 8) | bis.read());
					
					// 读取DHT段
					byte[] DHT = new byte[DHTLength - 2];
					if (bis.read(DHT) != DHTLength - 2) {
						LAST_ERROR = ERRCODE.RESOLVE_ERROR;
						bis.close();
						return null;
					}
					
					// 解析DHT段
					int pos = 0;	// 下一个读取数组位置标记
					HuffmanTable huffmanTable = new HuffmanTable();				// 单个哈夫曼表
					while (pos < DHT.length) {
						// 获取哈夫曼表类型
						huffmanTable.type = DHT[pos++];
						
						// 计算总叶子结点数量
						int codeCount = 0;
						for (int i = 0; i < 16; i++) {
							codeCount += DHT[pos + i] & 0xFF;
						}
						
						huffmanTable.points = new HuffmanTableLeafPoint[codeCount];	// 初始化叶子节点
						int curLeafPoint = 0, 		// 当前处理的叶子节点
							curCodeLengthCount = 0; // 当前长度的码字数量
						
						// 构造哈夫曼表
						for (int curCodeLength = 1; curCodeLength <= 16; curCodeLength++) {
							// 获取当前长度的码字数量
							curCodeLengthCount = DHT[pos + curCodeLength - 1] & 0xFF;
							
							// 根据当前长度的码字数量生成对应数量的叶子节点
							for (int j = 0; j < curCodeLengthCount; j++) {
								huffmanTable.points[curLeafPoint] = new HuffmanTableLeafPoint();
								huffmanTable.points[curLeafPoint].code = new int[curCodeLength];
								for (int k = 0; k < curCodeLength; k++) {
									huffmanTable.points[curLeafPoint].code[k] = 0;
								}
								
								// 非第一个码字
								if (curLeafPoint > 0) {
									boolean plusFlag = false; // 已处理码字加一标志
									for (int i = huffmanTable.points[curLeafPoint - 1].code.length - 1; i >= 0; i--) {
										if (!plusFlag) {
											if (huffmanTable.points[curLeafPoint - 1].code[i] == 0) {
												huffmanTable.points[curLeafPoint].code[i] = 1;
												plusFlag = true;
											} else {
												huffmanTable.points[curLeafPoint].code[i] = 0;
											}
										} else {
											huffmanTable.points[curLeafPoint].code[i] = huffmanTable.points[curLeafPoint - 1].code[i];
										}
									}
								}
								huffmanTable.points[curLeafPoint].value = DHT[pos + 16 + curLeafPoint];
								curLeafPoint++;
							}
						}
						
						// 保存当前哈夫曼表调整位置到下一个哈夫曼表（如果存在）
						huffmanTables.add(huffmanTable);
						huffmanTable = new HuffmanTable();
						pos = pos + codeCount + 16; 
					}
				} else if (temp == 0xDA) {
					break;
				}
			}
		}
		if (huffmanTables.isEmpty()) {
			LAST_ERROR = ERRCODE.FORMAT_ERROR;
			bis.close();
			return null;
		}
		bis.close();
		return huffmanTables;
	}
	
	// 将单个行程编码解析为数值
	private static int decodeRLE(int[] code) {
		int decode = 0;
		if (code.length == 0) {
			return 0;
		}
		if (code[0] == 0) {
			for (int i = 0; i < code.length; i++) {
				if (code[i] != 0 && code[i] != 1) {
					return 0;
				} else {
					decode = (decode << 1) + Math.abs(code[i] - 1);
				}

			}
			decode = -decode;
		} else {
			for (int i = 0; i < code.length; i++) {
				if (code[i] != 0 && code[i] != 1) {
					return 0;
				} else {
					decode = (decode << 1) + code[i];
				}
			}
		}
		return decode;
	}
	
	// 将单个行程编码解析为数值
		private static int decodeRLE(ArrayList<Integer> code) {
			int decode = 0;
			if (code.size() == 0) {
				return 0;
			}
			if (code.get(0) == 0) {
				for (int i = 0; i < code.size(); i++) {
					if (code.get(i) != 0 && code.get(i) != 1) {
						return 0;
					} else {
						decode = (decode << 1) + Math.abs(code.get(i) - 1);
					}

				}
				decode = -decode;
			} else {
				for (int i = 0; i < code.size(); i++) {
					if (code.get(i) != 0 && code.get(i) != 1) {
						return 0;
					} else {
						decode = (decode << 1) + code.get(i);
					}
				}
			}
			return decode;
		}
	
	// 打印JPG编码数据信息（测试用途）
	public static void printPicMatrixInfo(String filePath) throws IOException {
		BitInputStream bis = new BitInputStream(filePath);
		int[] buf = new int[8];
		while (bis.available() > 0) {
			bis.read(buf);
			if (Arrays.equals(buf, new int[] {1,1,1,1,1,1,1,1})) {
				bis.read(buf);
				if (Arrays.equals(buf, new int[] {1,1,0,1,1,0,1,0})) {
					int temp = 0;
					for (int i = 0; i < 16; i++) {
						temp = (temp << 1) | bis.read();
					}
					bis.skip((temp - 2) * 8);
					temp = 0;
					for (int i = 0; i < 320; i++) {
						System.out.print(bis.read());
						if ((++temp) % 4 == 0) {
							System.out.print(" ");
							if (temp % 32 == 0) {
								System.out.println();
							}
						}
					}
					return;
				}
			}
		}
	}
	
	// 解析JPG数据
	private ArrayList<MCUMatrix> decodeSOS(String filePath) throws IOException {
		// 判断是否支持此JPG图片采样精度格式
		switch (getSOF0Accuracy(filePath)) {
		case YCrCb411:
			break;
		case YCrCb111:
			LAST_ERROR = ERRCODE.FORMAT_ERROR;
			return null;
		default:
			return null;
		}
		
		// 获取哈夫曼表
		ArrayList<HuffmanTable> huffmanTables = null;
		if ((huffmanTables = getHuffmanTable(filePath)) == null)
			return null;
		
		// 获取SOS设置信息
		MCUTableSetting[] MCUTableSettings = getMCUTableSettings(filePath);
		
		// 获取RST间隔信息
		int RSTValue = getRST(filePath);
		if (RSTValue == -1)
			return null;
		
		// 跳转到数据段
		BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(filePath));
		while (bufferedInputStream.available() > 0) {
			if (bufferedInputStream.read() == 0xFF) {
				if (bufferedInputStream.read() == 0xDA) {
					bufferedInputStream.skip(12);
					break;
				}
			}
		}
		
		// 提取数据段并替换特殊标志
		ArrayList<Byte> buf = new ArrayList<>();
		while (bufferedInputStream.available() > 0) {
			buf.add((byte) bufferedInputStream.read());
			if ((buf.get(buf.size() - 1) & 0xFF) == 0xFF) {
				int temp = bufferedInputStream.read();
				while ((byte) temp == 0xFF) 
					// 读到0xFFFF，舍弃前一个FF，对后一个再做判断
					temp = bufferedInputStream.read();
				switch (temp) {
					case 0x00:
						// 读到0xFF00，进行编码替换
						break;
					case 0xD9:
						// 读到0xFFD9，数据段结束
						buf.remove(buf.size() - 1);
						bufferedInputStream.skip(bufferedInputStream.available());
						break;
					default:
						if (temp >= 0xD0 && temp <= 0xD7)
							// 读到0xFFD0~D7，为RST标记，忽略
							buf.remove(buf.size() - 1);
						else
							// 读到0xFF??，舍弃FF，对随后的数值进行译码
							buf.remove(buf.size() - 1);
							buf.add((byte) temp);
						break;
				}
			}
		}
		bufferedInputStream.close();
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("tempfile.tmp"));
		for (Byte bytedata : buf) 
			bos.write(bytedata);
		bos.close();
		
		// 解析数据段
		BitInputStream bis = new BitInputStream("tempfile.tmp");
		ArrayList<MCUMatrix> MCUMatrixs = new ArrayList<>();
		int YCount = 0, curMatrixPos = 0;
		MCUMatrix matrix = new MCUMatrix();
		matrix.type = MCUType.Y;
		ArrayList<Integer> codeCount = new ArrayList<>();
		HuffmanTable usingDCTable = null, usingACTable = null;
		for (MCUTableSetting setting : MCUTableSettings) {
			if (setting.type == matrix.type) {
				for (HuffmanTable huffmanTable : huffmanTables) {
					if (huffmanTable.type == setting.DCTable)
						usingDCTable = huffmanTable;
					else if (huffmanTable.type == setting.ACTable)
						usingACTable = huffmanTable;
				}
				break;
			}
		}
		while (bis.available() > 0) {
			boolean notFind = true;
			while (notFind) {
				codeCount.add(bis.read());
				if (codeCount.size() >= 32) {
					LAST_ERROR = ERRCODE.RESOLVE_ERROR;
					bis.close();
					File file = new File("tempfile.tmp");
					if (file.exists())
						file.delete();
					return null;
				}
				if (curMatrixPos == 0) {
					for (HuffmanTableLeafPoint point : usingDCTable.points) {
						if (point.code.length == codeCount.size()) {
							notFind = false;
							for (int i = 0; i < codeCount.size(); i++) {
								if (point.code[i] != codeCount.get(i)) {
									notFind = true;
									break;
								}
							}
							if (!notFind) {
								codeCount = new ArrayList<>();
								for (int i = 0; i < point.value; i++)
									codeCount.add(bis.read());
								matrix.matrix[curMatrixPos++] = decodeRLE(codeCount);
								codeCount = new ArrayList<>();
								break;
							}
						} else if (point.code.length > codeCount.size())
							break;
					}
				} else {
					for (HuffmanTableLeafPoint point : usingACTable.points) {
						if (point.code.length == codeCount.size()) {
							notFind = false;
							for (int i = 0; i < codeCount.size(); i++) {
								if (point.code[i] != codeCount.get(i)) {
									notFind = true;
									break;
								}
							}
							if (!notFind) {
								for (int i = 0; i < ((point.value >> 4) & 0x0F); i++) 
									matrix.matrix[curMatrixPos++] = 0;		// 此处有疑问，为什么游长码的0填充数量会超过矩阵的大小64，超过了要怎么办？
								if ((point.value & 0x0F) == 0) {
									for (int i = curMatrixPos; i < 64; i++)
										matrix.matrix[i] = 0;
									curMatrixPos = 0;
									codeCount = new ArrayList<>();
								} else {
									codeCount = new ArrayList<>();
									for (int i = 0; i < point.value; i++)
										codeCount.add(bis.read());
									matrix.matrix[curMatrixPos++] = decodeRLE(codeCount);
									codeCount = new ArrayList<>();
									if (curMatrixPos == 64)
										curMatrixPos = 0;
								}
								break;
							}
						} else if (point.code.length > codeCount.size())
							break;
					}
				}
			}
			if (curMatrixPos == 0) {
				MCUMatrixs.add(matrix);
				if (matrix.type == MCUType.Y) {
					matrix = new MCUMatrix();
					if (++YCount == 4) {
						matrix.type = MCUType.Cr;
						usingDCTable = null;
						usingACTable = null;
						for (MCUTableSetting setting : MCUTableSettings) {
							if (setting.type == matrix.type) {
								for (HuffmanTable huffmanTable : huffmanTables) {
									if (huffmanTable.type == setting.DCTable)
										usingDCTable = huffmanTable;
									else if (huffmanTable.type == setting.ACTable)
										usingACTable = huffmanTable;
								}
								break;
							}
						}
					} else
						matrix.type = MCUType.Y;
				} else if (matrix.type == MCUType.Cr) {
					matrix = new MCUMatrix();
					YCount = 0;
					matrix.type = MCUType.Cb;
					usingDCTable = null;
					usingACTable = null;
					for (MCUTableSetting setting : MCUTableSettings) {
						if (setting.type == matrix.type) {
							for (HuffmanTable huffmanTable : huffmanTables) {
								if (huffmanTable.type == setting.DCTable)
									usingDCTable = huffmanTable;
								else if (huffmanTable.type == setting.ACTable)
									usingACTable = huffmanTable;
							}
							break;
						}
					}
				} else if (matrix.type == MCUType.Cb) {
					matrix = new MCUMatrix();
					matrix.type = MCUType.Y;
					usingDCTable = null;
					usingACTable = null;
					for (MCUTableSetting setting : MCUTableSettings) {
						if (setting.type == matrix.type) {
							for (HuffmanTable huffmanTable : huffmanTables) {
								if (huffmanTable.type == setting.DCTable)
									usingDCTable = huffmanTable;
								else if (huffmanTable.type == setting.ACTable)
									usingACTable = huffmanTable;
							}
							break;
						}
					}
				}
			}
		}
		File file = new File("tempfile.tmp");
		if (file.exists())
			file.delete();
		bis.close();
		return MCUMatrixs;
	}

	// 读取DRI段中有关RST的信息
	private int getRST(String filePath) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath));
		if (bis.read() != 0xFF || bis.read() != 0xD8) {
			LAST_ERROR = ERRCODE.FORMAT_ERROR;
			bis.close();
			return -1;
		}
		while (bis.available() > 0) {
			if (bis.read() == 0xFF) {
				int temp = bis.read();
				if (temp == 0xDD) {
					// 跳转到RST间隔信息位置
					bis.skip(2);
					
					// 解析RST间隔信息
					temp = (bis.read() << 8) | bis.read();
					bis.close();
					return temp;
				} else if (temp == 0xDA) {
					bis.close();
					return 0;
				}
			}
		}
		LAST_ERROR = ERRCODE.RESOLVE_ERROR;
		bis.close();
		return -1;
	}
	
	// MCU单元分量类型
	enum MCUType {
		Y,
		Cr,
		Cb
	}
	
	// 采样精度
	enum Accuracy {
		YCrCb411,
		YCrCb111
	}
	
	// 单个MCU8x8矩阵
	class MCUMatrix {
		MCUType type;
		int[] matrix = new int[64];
	}
	
	// MCU分量使用的哈夫曼表编号
	class MCUTableSetting {
		MCUType type;
		byte DCTable, ACTable;
	}
	
	// 读取SOF0段中有关采样精度的信息
	private Accuracy getSOF0Accuracy(String filePath) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath));
		if (bis.read() != 0xFF || bis.read() != 0xD8) {
			LAST_ERROR = ERRCODE.FORMAT_ERROR;
			bis.close();
			return null;
		}
		while (bis.available() > 0) {
			if (bis.read() == 0xFF) {
				if (bis.read() == 0xC0) {
					// 跳转到采样精度信息位置
					bis.skip(9);
					
					// 解析采样精度信息
					switch (bis.read()) {
					case 0x22:
						bis.close();
						return Accuracy.YCrCb411;
					case 0x11:
						bis.close();
						return Accuracy.YCrCb111;
					default:
						LAST_ERROR = ERRCODE.RESOLVE_ERROR;
						bis.close();
						return null;
					}
				}
			}
		}
		LAST_ERROR = ERRCODE.RESOLVE_ERROR;
		bis.close();
		return null;
	}
	
	// 读取SOS段首有关颜色分量与其使用的哈夫曼表号的信息
	private MCUTableSetting[] getMCUTableSettings(String filePath) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath));
		if (bis.read() != 0xFF || bis.read() != 0xD8) {
			LAST_ERROR = ERRCODE.FORMAT_ERROR;
			bis.close();
			return null;
		}
		MCUTableSetting[] MCUTableSettings = new MCUTableSetting[3];	// 哈夫曼表集合
		while (bis.available() > 0) {
			if (bis.read() == 0xFF) {
				if (bis.read() == 0xDA) {
					// 跳转到颜色分量信息位置
					bis.skip(3);
					
					// 解析颜色分量信息
					for (int i = 0; i < 3; i++) {
						MCUTableSettings[i] = new MCUTableSetting();
						
						// 读取分量类型
						switch (bis.read()) {
						case 1:
							MCUTableSettings[i].type = MCUType.Y;
							break;
						case 2:
							MCUTableSettings[i].type = MCUType.Cr;
							break;
						case 3:
							MCUTableSettings[i].type = MCUType.Cb;
							break;
						default:
							LAST_ERROR = ERRCODE.RESOLVE_ERROR;
							bis.close();
							return null;
						}
						
						// 读取所使用的直流表号和交流表号
						byte temp = (byte) bis.read(), DC = (byte) ((temp >> 4) & 0x0F), AC = (byte) (temp & 0x0F);
						switch (DC & 0xFF) {
						case 0:
							MCUTableSettings[i].DCTable = HuffmanTableType.DC0;
							break;
						case 1:
							MCUTableSettings[i].DCTable = HuffmanTableType.DC1;
							break;
						default:
							LAST_ERROR = ERRCODE.RESOLVE_ERROR;
							bis.close();
							return null;
						}
						switch (AC & 0xFF) {
						case 0:
							MCUTableSettings[i].ACTable = HuffmanTableType.AC0;
							break;
						case 1:
							MCUTableSettings[i].ACTable = HuffmanTableType.AC1;
							break;
						default:
							LAST_ERROR = ERRCODE.RESOLVE_ERROR;
							bis.close();
							return null;
						}
					}
				}
			}
		}
		bis.close();
		return MCUTableSettings;
	}
		
	@Override
	protected void steganography(String outPut) throws IOException {
		// TODO 自动生成的方法存根

	}

	@Override
	protected void unSteganography(String outPut) throws IOException {
		// TODO 自动生成的方法存根

	}
}