package com.togogo.config;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import com.togogo.annotation.RecordDemo;
import com.togogo.controller.TestController;

@Configuration
@EnableSpringConfigured
@ComponentScan(basePackages = "com.togogo.*")
@EnableWebMvc
@EnableAspectJAutoProxy
@EnableTransactionManagement
public class SpringConfig {

	// Oracle
	@Value(value = "oracle.jdbc.driver.OracleDriver")
	private String driverClassName = "oracle.jdbc.driver.OracleDriver";
	@Value(value = "jdbc:oracle:thin:@localhost:1521:orcl")
	private String url = "jdbc:oracle:thin:@localhost:1521:orcl";
	@Value(value = "scott")
	private String username = "scott";
	@Value(value = "tiger")
	private String password = "tiger";

	// MySQL
	// private String driverClassName="com.mysql.jdbc.Driver";
	// private String url="jdbc:mysql://localhost:3306/exam_system";
	// private String username="root";
	// private String password="123456";

	@Bean
	@Scope(value = BeanDefinition.SCOPE_SINGLETON)
	public InternalResourceViewResolver jspViewResolver() {
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setViewClass(JstlView.class);
		resolver.setPrefix("/WEB-INF/views/");
		resolver.setSuffix(".jsp");
		return resolver;
	}

	@Bean
	public CommonsMultipartResolver multipartResolver() {
		CommonsMultipartResolver resolver = new CommonsMultipartResolver();
		resolver.setDefaultEncoding("UTF-8");
		return resolver;
	}

	@Bean(name = "dataSource")
	// @Scope(value=BeanDefinition.SCOPE_PROTOTYPE)
	public DataSource dataSource() {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(driverClassName);
		dataSource.setUrl(url);
		dataSource.setUsername(username);
		dataSource.setPassword(password);
		return dataSource;
	}

	@Bean(name = "transactionManager")
	public DataSourceTransactionManager transactionManager() {
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
		transactionManager.setDataSource(this.dataSource());
		return transactionManager;
	}

	@Bean(name = "sqlSessionFactory")
	public SqlSessionFactoryBean sqlSessionFactory() {
		System.out.println("----------------sqlSessionFactory-------------------");
		SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
		factory.setDataSource(this.dataSource());
		return factory;
	}
	
	@Bean
	// @Scope(value = BeanDefinition.SCOPE_SINGLETON)
	public MapperScannerConfigurer mapperScannerConfigurer() {
		System.out.println("-------------mapperScannerConfigurer-----------------");
		MapperScannerConfigurer configurer = new MapperScannerConfigurer();
		configurer.setSqlSessionFactoryBeanName("sqlSessionFactory");
		configurer.setAnnotationClass(Repository.class);
		configurer.setBasePackage("com.togogo.mapper");
		return configurer;
	}

	@Bean
	public TestController testController() {
		return new TestController();
	}
	
	@Bean(name = "recordFunction")
	public RecordDemo recordDemo() {
		RecordDemo recordDemo = new RecordDemo();
		recordDemo.setModelBasePackage("com.togogo.dto");
		return recordDemo;
	}

}
