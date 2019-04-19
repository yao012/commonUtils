package common;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * @author Administrator
 */
public class VersionUtils {

	/**
	 * 比较两个字符串形式的版本号的大小
	 * @param v1
	 * @param v2
	 * @return -1表示前者小于后者   0表示等于   1表示大于
	 * 1.1.1	1.1.01
	 */
	public static int compare(String v1, String v2){

		if(StringUtils.isBlank(v1) || StringUtils.isBlank(v2)){
			return 0;
		}

		int i=0,j=0,x=0,y=0;
		int v1Len = v1.length(); 
		int v2Len = v2.length(); 
		char c;
		do{
			while(i<v1Len){//计算出v1中的点之前的数字
				c = v1.charAt(i++);
				if(c>='0' && c<='9'){
					x = x*10 + (c-'0');
				}else if(c=='.'){
					break;//结束
				}else{
					//无效的字符
				}
			}
			while(j<v2Len){//计算出v2中的点之前的数字
				c = v2.charAt(j++);
				if(c>='0' && c<='9'){
					y = y*10 + (c-'0');
				}else if(c == '.'){
					break;//结束
				}else{
					//无效的字符
				}
			}
			if(x<y){
				return -1;
			}else if(x>y){
				return 1;
			}else{
				x=0; y=0;
				continue;
			}
		}while((i<v1Len) || (j<v2Len));
		return 0;
	}

	/**
	 *  测试当前版本是否需要升级
	 * @param currentVersion
	 * @param lastVersion
	 * @param lastVnum
	 */
	public static boolean needUpgrade(String currentVersion,String lastVersion,int lastVnum){
		boolean needUpgrade = true;
		if(currentVersion != null){
			if(currentVersion.contains(".")){
				if(compare(currentVersion, lastVersion)>=0){
					//客户端的版本已经是最新的了
					needUpgrade = false;
				}
			}else{//客户端使用的是整数形式的版本号
				int v = Integer.parseInt(currentVersion);
				if(v>=lastVnum){
					//客户端的版本已经是最新的了
					needUpgrade = false;
				}
			}
		}

		return needUpgrade;
	}

	/** 创建一个xml对象 */
	public static Document createXml(int vnum,String fname,String brief,String urlPath,String urlPath2 ,String urlPath3,
								 String urlPath4,String verText) throws Exception{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		int index = 0;
		String after = null;
		DocumentBuilder builder;
		Element name = null;
		Element name2 = null;
		Element name3 = null;
		Element name4 = null;

		builder = factory.newDocumentBuilder();
		Document document = builder.newDocument();
		Element root = document.createElement("upgrade");
		document.appendChild(root);


		Element version = document.createElement("version");
		version.appendChild(document.createTextNode("" + vnum));

		name = document.createElement("name");
		name.appendChild(document.createTextNode("" + fname));

		if(urlPath2 != null){
			index = urlPath2.lastIndexOf("/");
			after = urlPath2.substring(index+1);
			name2 = document.createElement("name2");
			name2.appendChild(document.createTextNode(""+ after));
		}

		if(urlPath3 != null){
			index = urlPath3.lastIndexOf("/");
			after = urlPath3.substring(index+1);
			name3 = document.createElement("name3");
			name3.appendChild(document.createTextNode(""+ after));
		}

		if(urlPath4 != null){
			index = urlPath4.lastIndexOf("/");
			after = urlPath4.substring(index+1);
			name4 = document.createElement("name4");
			name4.appendChild(document.createTextNode(""+ after));
		}

		Element detail = document.createElement("detail");
		detail.appendChild(document.createTextNode("" + brief));

		Element url = document.createElement("url");

		url.appendChild(document.createTextNode("" + urlPath));

		Element url2 = document.createElement("url2");

		url2.appendChild(document.createTextNode("" + urlPath2));

		Element url3 = document.createElement("url3");

		url3.appendChild(document.createTextNode("" + urlPath3));

		Element url4 = document.createElement("url4");

		url4.appendChild(document.createTextNode("" + urlPath4));

		root.appendChild(version);
		root.appendChild(name);

		if(name2 != null){
			root.appendChild(name2);
		}

		if(name3 != null){
			root.appendChild(name3);
		}

		if(name4 != null){
			root.appendChild(name4);
		}

		root.appendChild(detail);
		root.appendChild(url);
		root.appendChild(url2);
		root.appendChild(url3);
		root.appendChild(url4);

		Element vtext = document.createElement("vtext");
		vtext.appendChild(document.createTextNode("" + verText));
		root.appendChild(vtext);

		return document;
	}


	public static String createJSONResult(int vnum,String fname,String brief,String urlPath,String urlPath2 ,
										  String urlPath3, String urlPath4,String verText,String comment){

		JSONObject json = createJSONResultObj(vnum,fname,brief,urlPath,urlPath2,urlPath3,urlPath4,verText,comment);
		JSONObject root = new JSONObject();
		root.put("upgrade", json);
		return root.toString();
	}

	public static JSONObject createJSONResultObj(int vnum,String fname,String brief,String urlPath,String urlPath2 ,
										  String urlPath3, String urlPath4,String verText,String comment){

		int index = 0;
		String after = null;
		JSONObject json = new JSONObject();
		json.put("version", vnum);
		json.put("vtext", verText);
		json.put("name", fname);

		if(urlPath2 != null){
			index = urlPath2.lastIndexOf("/");
			after = urlPath2.substring(index+1);
			json.put("name2", after);
		}

		if(urlPath3 != null){
			index = urlPath3.lastIndexOf("/");
			after = urlPath3.substring(index+1);
			json.put("name3", after);
		}

		if(urlPath4 != null){
			index = urlPath4.lastIndexOf("/");
			after = urlPath4.substring(index+1);
			json.put("name4", after);
		}

		json.put("detail", brief);
		json.put("comment", comment);
		json.put("url", urlPath);
		json.put("url2", urlPath2);
		json.put("url3", urlPath3);
		json.put("url4", urlPath4);
		return json;
	}

	/**
	 *
	 * @param vnum
	 * @param lastVnum
	 * @param vtext
	 * @param lastVtext
	 * @return true 表示需要升级 ,false 不需要升级
	 */
	public static boolean checkupByVnumOrVtext(int vnum, int lastVnum, String vtext, String lastVtext) {
		if (vtext == null) {
			if (vnum < lastVnum) {
				return true;
			}
		} else {
			if (compare(vtext, lastVtext) == -1) {
				return true;
			}
		}
		return false;
	}


}
