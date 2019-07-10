package common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 代表本地挂载的文件系统，仅支持 CentOS 和 Ubuntu Linux 系统，其它系统正确性未知。
 * <p>
 * <p>
 * 支持查询挂载设备名、挂载点、文件类型信息。
 * <p>
 * <p>
 * 不支持含有空白符的挂载点！！！
 * <p>
 *     <ol>
 *         <li>使用 <code>echo $(readlink -f /dev/disk/by-uuid/{uuid}) </code>指令找到相应的设备名</li>
 *         <li>使用 <code>blkid /dev/{device} </code>指令找到相应的设备 UUID</li>
 *     </ol>
 * <p>
 * <p>
 * <a href="https://help.ubuntu.com/community/UsingUUID">Using UUID</a><a href="https://wiki.debian.org/Part-UUID">使用 UUID 作为磁盘标识的好处</a>
 * <a href="https://www.centos.org/forums/viewtopic.php?t=16647">使用 UUID 作为磁盘标识的好处</a>
 *
 * @author Jerry Chin
 */
public class FileSystemUtil {
	private final static Logger L = LogManager.getLogger(FileSystemUtil.class);


	/**
	 * 保存当前所有挂载的文件系统信息
	 * <p>
	 * <p>
	 * <p>
	 * 文件格式同 <a href="http://www.man7.org/linux/man-pages/man5/fstab.5.html">fstab</a>
	 * <p>
	 * <ol>
	 * <li>deviceName 挂载设备名（目前比较关心）</li>
	 * <li>mountPoint 挂载点名称 （目前比较关心）</li>
	 * <li>fsType 文件系统类型</li>
	 * <li>fs_mntops 文件系统相关的挂载参数</li>
	 * <li>fs_freq 导出文件系统</li>
	 * <li>fs_passno 文件系统检查顺序 </li>
	 * </ol>
	 * <p>
	 * <p>
	 * <p>
	 * 此文件是 `proc` 虚拟文件系统的一部分，并非存放在磁盘上的真实文件。
	 * <p>
	 * <p>
	 * <p>
	 * <a href="http://manpages.ubuntu.com/manpages/precise/man5/proc.5.html">支持 Ubuntu</a>
	 * <a href="https://access.redhat.com/documentation/en-US/Red_Hat_Enterprise_Linux/4/html/Introduction_To_System_Administration/s4-storage-mounting-proc.html">
	 * 支持 RedHat/CentOS</a>
	 */
	private final static File PROC_MOUNTS = new File("/proc/mounts");
	private final static Path UUID_DIRECTORY = Paths.get("/dev/disk/by-uuid/");


	public static class FileSystem {
		private final String deviceName;
		private final String mountPoint;

		private FileSystem(final String deviceName,
		                   final String mountPoint) {
			this.deviceName = deviceName;
			this.mountPoint = mountPoint;
		}

		public String getDeviceName() {
			return deviceName;
		}

		public String getMountPoint() {
			return mountPoint;
		}

		@Override
		public String toString() {
			return "FileSystem{" +
					"deviceName='" + deviceName + '\'' +
					", mountPoint='" + mountPoint + '\'' +
					'}';
		}
	}


	/**
	 * 返回该磁盘唯一标识符本次映射的设备名称
	 */
	public static String uuid2DeviceName(String uuid) {
		try {
			return UUID_DIRECTORY.resolve(uuid).toRealPath().toString();
		} catch (IOException e) {
			L.error("resolving disk uuid {} failed. \nplease make sure the disk is attached correctly. hint: use blkid and check {}",
					uuid, UUID_DIRECTORY, e);
			throw new RuntimeException("resolving disk uuid " + uuid + " failed.", e);
		}

	}

	/**
	 * 返回所有挂载设备信息
	 *
	 * @return key 挂载设备名
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, FileSystem> listMountedFS() {
		String osName = System.getProperty("os.name").toLowerCase();

		// 平台检查
		if (!osName.startsWith("linux")) {
			L.error("can't list mounted FS on non-linux OS ({})", osName);
			throw new UnsupportedOperationException("can't list mounted FS on non-linux OS (" + osName + ")");
		}

		L.info("os.name: {}", osName);

		// 不扩容的情况下，支持存放 4 < ( 6 * 0.75) 个文件系统信息
		Map<String, FileSystem> fileSystems = new HashMap<>(6);

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(PROC_MOUNTS), Charset.forName("US-ASCII")))) {

			String line;
			while ((line = reader.readLine()) != null) {

				// 返回很多非真实设备的挂载信息，仅关注 /dev 目录下的设备
				if (!line.startsWith("/dev")) {
					L.info("only interested in device under /dev, skip {}...", StringUtil.truncate(line, 20));
					continue;
				}

				Scanner scanner = new Scanner(line);
				List<String> fields = new ArrayList<>(6);
				while (scanner.hasNext()) {
					fields.add(scanner.next());
				}

				if (fields.size() < 3) {
					L.fatal("malformed mount info: {}", line);
					continue;
				}

				fileSystems.put(fields.get(0), new FileSystem(fields.get(0), fields.get(1)));
			}

			return fileSystems;
		} catch (IOException e) {
			L.error("reading mounted FS list failed. hint: check whether /proc/mounts is readable or not.", e);

			throw new RuntimeException("reading mounted FS list failed.", e);
		}
	}
}
