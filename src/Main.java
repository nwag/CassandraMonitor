import java.time.Instant;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * 
 * @author nicolas wagner
 *
 */
public class Main{

	// define connection and sampling interval
	private final static String SERVER_IP = "1.2.3.4";
	private final static int SERVER_PORT = 7199;
	private final static int SAMPLETIME_IN_MS = 5000;

	private static Logger logger = Logger.getLogger("CassandraStats");
	private static FileHandler fh;

	public static void main( String[] args ) {
		try {
			JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://"+SERVER_IP+":"+SERVER_PORT+"/jmxrmi");
			JMXConnector jmxc = JMXConnectorFactory.connect(url, null); 
			MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

			long unixTimestamp = Instant.now().getEpochSecond();
			fh = new FileHandler(System.getProperty("user.dir")+"/stats"+unixTimestamp+".log");  
			logger.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();  
			fh.setFormatter(formatter);  

			ObjectName attribute0 = new ObjectName("org.apache.cassandra.metrics:type=ColumnFamily,name=TotalDiskSpaceUsed");
			ObjectName attribute1 = new ObjectName("org.apache.cassandra.metrics:type=ColumnFamily,name=ReadLatency");
			ObjectName attribute2 = new ObjectName("org.apache.cassandra.metrics:type=ColumnFamily,name=WriteLatency");
			ObjectName attribute3 = new ObjectName("org.apache.cassandra.metrics:type=Client,name=connectedNativeClients");
			ObjectName attribute4 = new ObjectName("org.apache.cassandra.metrics:type=Client,name=connectedThriftClients");

			String attribute0Unit = "byte";
			String attribute1Unit =  mbsc.getAttribute(attribute1, "DurationUnit").toString();
			String attribute2Unit =  mbsc.getAttribute(attribute2, "DurationUnit").toString();

			while(true){
				String attribute0Status =  mbsc.getAttribute(attribute0, "Value").toString();
				String attribute1Status =  mbsc.getAttribute(attribute1, "Mean").toString();
				String attribute2Status =  mbsc.getAttribute(attribute2, "Mean").toString();
				String attribute3Status =  mbsc.getAttribute(attribute3, "Value").toString();
				String attribute4Status =  mbsc.getAttribute(attribute4, "Value").toString();

				logger.info("TotalDiskSpaceUsed: "+attribute0Status+" "+attribute0Unit+"\nReadLatency: "+attribute1Status+" "
						+attribute1Unit+"\nWriteLatency: "+ attribute2Status+" "+attribute2Unit+"\nconnectedNativeClients: "
						+attribute3Status+"\nconnectedThriftClients: "+attribute4Status);
				Thread.sleep(SAMPLETIME_IN_MS);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	} 
}
