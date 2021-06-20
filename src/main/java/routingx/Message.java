package routingx;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;
import routingx.json.JSON;

@Setter
@Getter
@JsonInclude(Include.NON_NULL)
public class Message {

	public static final Integer SUCCESS = 0;
	public static final Integer FAIL = 1;

	@Note("成功失败标识")
	private Integer code = SUCCESS;

	@Note("命令编码")
	private String command;

	@Note("游戏类型")
	private int gameType = 0;

	@Note("连接标识")
	private String ctxId;

	@Note("消息体")
	private Object data;

	@Note("提示信息")
	private String msg;

	@Note("生产者")
	private String producer;

	@Note("消费者")
	private String consumer;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date timestamp = new Date();

	private String id;

	public Message() {

	}

	public String cmd() {
		return command;
	}

	public boolean success() {
		return code == SUCCESS;
	}

	public void ok(String msg) {
		this.setCode(SUCCESS);
		this.setMsg(msg);
	}

	public void ok(String msg, Object data) {
		this.ok(msg);
		this.setData(data);
	}

	public void fail(String msg) {
		this.setCode(FAIL);
		this.setMsg(msg);
	}

	@Override
	public String toString() {
		return System.lineSeparator() + JSON.toJSONString(this, true);
	}
}
