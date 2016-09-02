package com.togogo.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherMapper {
	
	@Select(value="SELECT * FROM tb_teacher")
	@ResultType(value=Map.class)
	List<Map<String,Object>> queryAll();

}
