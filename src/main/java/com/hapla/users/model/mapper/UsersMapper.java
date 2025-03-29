package com.hapla.users.model.mapper;

import com.hapla.admin.model.vo.DashBoard;
import com.hapla.users.model.vo.Users;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;

@Mapper
public interface UsersMapper {
    int checkUser(Users user);

    Users login(Users user);

    int join(Users user);

    int checkNickname(String nickname);

    int updateUser(Users user);

    int deleteUser(int no);

    ArrayList<String> selectPlaceId(int userNo);

	ArrayList<DashBoard> selectAllLog();

	int insertLog(HashMap<String, String> map);
}
