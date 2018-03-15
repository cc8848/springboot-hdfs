package com.embraces.hdfs.config;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Kerberos认证
 * @author	tokings.tang@embracesource.com
 * @date	2018年3月6日 下午3:47:35
 * @copyright	http://www.embracesource.com
 */
@Configuration
public class KerberosConfig {
	
	private Logger log = LoggerFactory.getLogger(KerberosConfig.class);

	/**
	 * 认证类型
	 */
	@Value("${authentication.type}")
	private String authenticationType;
	
	/**
	 * 认证服务器配置文件路径
	 */
	@Value("${authentication.kerberos.krb5FilePath}")
	private String krb5FilePath;
	
	/**
	 * 认证主体
	 */
	@Value("${authentication.kerberos.principal}")
	private String principal;
	
	/**
	 * 认证使用的keytab文件
	 */
	@Value("${authentication.kerberos.keytab}")
	private String keytab;

	/**
	 * 认证
	 */
	@PostConstruct
	private void authentication() {

		if(authenticationType != null && "kerberos".equalsIgnoreCase(authenticationType.trim())) {
			log.info("开始设置kerberos身份验证.");
		} else {
			log.info("未设置kerberos身份验证.");
			return;
		}
		
		if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
			// 默认：这里不设置的话，win默认会到 C盘下读取krb5.init
			System.setProperty("java.security.krb5.conf", krb5FilePath);
		} else {
			// linux 会默认到 /etc/krb5.conf
			// 中读取krb5.conf,本文笔者已将该文件放到/etc/目录下，因而这里便不用再设置了
			System.setProperty("java.security.krb5.conf", krb5FilePath); 
		}

		// 使用Hadoop安全登录
		org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();
		 conf.set("hadoop.security.authentication", authenticationType);
		try {
			UserGroupInformation.setConfiguration(conf);
			UserGroupInformation.loginUserFromKeytab(principal, keytab);
			log.info("Kerberos身份认证完成, krb5FilePath：{}, principal:{}, keytab:{}", krb5FilePath, principal, keytab);
		} catch (IOException e1) {
			log.error(e1.getMessage() + ", detail:{}", e1);
		}
	}

}