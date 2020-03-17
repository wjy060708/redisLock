package com.wangjinyin.study.controller;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wangjinyin.study.bean.User;
import com.wangjinyin.study.service.UserService;

@RestController
public class UserController {
	
	@Autowired
	private UserService userService;
	
	//模拟2000的并发量
	@RequestMapping("/findAll")
	public void findUser() {
		
		//栅栏锁
		int  j = 0;
		CyclicBarrier cyclicBarrier = new CyclicBarrier(20);  //栅栏锁 模拟2000个并发
		for (int i = 0; i < 2000; i++) {
			new Thread(()-> {
				try {
					cyclicBarrier.await();
					User user = userService.getUser(1l);
					System.out.println(user.getName() + ": "+ Thread.currentThread().getName());
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (BrokenBarrierException e) {
					e.printStackTrace();
				}
			}).start();
		}
	} 
}
