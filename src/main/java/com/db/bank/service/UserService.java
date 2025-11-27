package com.db.bank.service;

import com.db.bank.apiPayload.exception.UserException;
import com.db.bank.domain.entity.User;
import com.db.bank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;

//    //회원가입(유저생성)
//    public User createUser(String userId, String password, String name){
//        //우선 pw String
//        if(userRepository.existsByUserId(userId)){
//            throw new UserException.UserAlreadyExistsException("이미 존재하는 아이디: "+userId);
//        }
//        User user=User.builder()
//                .userId(userId)
//                .password(password)
//                .name(name)
//                .build();
//        return userRepository.save(user);
//    }
//
//    //로그인
//    @Transactional(readOnly = true)
//    public User login(String userId, String password){
//        return userRepository.findByUserIdAndPassword(userId, password)
//                .orElseThrow(()-> new UserException.InvalidLoginException("아이디 또는 비밀번호가 일치하지 않습니다"));
//    }
//
//    //회원가입 아이디 중복체크
//    @Transactional(readOnly = true)
//    public boolean checkId(String userId) {
//
//        boolean exists = userRepository.existsByUserId(userId);
//
//        if (exists) {
//            throw new UserException.UserAlreadyExistsException(
//                    "이미 존재하는 아이디입니다: " + userId
//            );
//        }
//
//        return false; // 사용 가능
//    }




    //아이디로 사용자 조회
    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new UserException.UserNonExistsException("유저를 찾을 수 없습니다. id=" + id));
    }



}
