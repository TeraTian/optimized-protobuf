package cn.tera.protobuf.controller;

import cn.tera.protobuf.coder.encoder.BasicEncoder;
import cn.tera.protobuf.coder.models.java.CoderTestStudent;
import cn.tera.protobuf.coder.models.java.CoderTestStudent.Parent;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/protobuf")
public class ProtobufController {
    @Autowired
    private HttpServletResponse response;

    @GetMapping("/getStudent")
    public void getStudent() throws IOException {
        String source = "{\"age\":13,\"father\":{\"age\":45,\"name\":\"Tom\"},\"friends\":[\"mary\",\"peter\",\"john\"],\"hairCount\":342728123942,\"height\":180.3,\"hobbies\":[{\"cost\":130,\"name\":\"football\"},{\"cost\":270,\"name\":\"basketball\"}],\"isMale\":true,\"mother\":{\"age\":45,\"name\":\"Alice\"},\"name\":\"Tera\",\"weight\":52.34}";
//        String source = "{\"age\":13}";
        CoderTestStudent student = JSON.parseObject(source, CoderTestStudent.class);
        ServletOutputStream out = response.getOutputStream();
        byte[] result = BasicEncoder.serialize(student, CoderTestStudent.class);
        out.write(result);
    }
}
