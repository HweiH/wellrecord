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
	
	@RecordAnno(isParsing=true,title="����", text="��������շѶ���{0}���ٶ�{1}�����Ĺ淶����[{tailNo@2}]���ⷿ����{tailNos@2}")
	@RequestMapping(value="testRecordAnno.do")
	public ModelAndView testRecordAnno(String tailNo, String tailNos, TailDto tailDto) {
		
		System.out.println("��ȡ���ı����ǣ�" + tailNo);
		
		ModelAndView mv = new ModelAndView("login");
		mv.addObject("isLogin", false);
		mv.addObject("tailNo", tailNo);
		mv.addObject("tailNos", tailNos);
		mv.addObject("tailNoINTailDto", tailDto.getTailNo());
		mv.addObject("tailNosINTailDto", tailDto.getTailNos());
		
		return mv;
		
	}

}
