package com.togogo.controller;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.togogo.annotation.RecordAnno;
import com.togogo.dto.TailDto;

@Controller
@Scope("request")
public class TestController {
	
//	@Autowired
//	private TeacherMapper teacherMapper;
	
	@RequestMapping(value="test.do",method=RequestMethod.GET)
	public String test(){
		
		System.out.println("--Test--");
		
		return "login";
	}
	
	@RecordAnno(isParsing=true,title="测试", text="这个单独收费都是{0}多少度{1}发生的规范化的[{tailNo@2}]蛋糕房的人{tailNos@2}")
	@RequestMapping(value="testRecordAnno.do")
	public ModelAndView testRecordAnno(String tailNo, String tailNos, TailDto tailDto) {
		
		System.out.println("获取到的变量是：" + tailNo);
		
		ModelAndView mv = new ModelAndView("login");
		mv.addObject("isLogin", false);
		mv.addObject("tailNo", tailNo);
		mv.addObject("tailNos", tailNos);
		mv.addObject("tailNoINTailDto", tailDto.getTailNo());
		mv.addObject("tailNosINTailDto", tailDto.getTailNos());
		
		return mv;
		
	}

}
