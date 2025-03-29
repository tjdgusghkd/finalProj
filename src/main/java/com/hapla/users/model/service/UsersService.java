package com.hapla.users.model.service;

import com.hapla.admin.model.vo.DashBoard;
import com.hapla.users.model.mapper.UsersMapper;
import com.hapla.users.model.vo.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class UsersService {

    private final UsersMapper mapper;

    public int checkUser(Users user) {
        return mapper.checkUser(user);
    }

    public Users login(Users user) {
        return mapper.login(user);
    }

    public int join(Users user) {
        return mapper.join(user);
    }

    public int checkNickname(String nickname) {
        return mapper.checkNickname(nickname);
    }

    public int updateUser(Users user) {
        return mapper.updateUser(user);
    }

    public int deleteUser(int no) {
        return mapper.deleteUser(no);
    }

    public ArrayList<String> selectPlaceId(int userNo) {
        return mapper.selectPlaceId(userNo);
    }

	public ArrayList<DashBoard> selectAllLog() {
		return mapper.selectAllLog();
	}

	public int insertLog(HashMap<String, String> map) {
		return mapper.insertLog(map);
	}
}
