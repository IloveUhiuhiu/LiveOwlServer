package com.server.liveowl.controller;

import com.server.liveowl.dto.AccountDetailDTO;
import com.server.liveowl.dto.TokenDTO;
import com.server.liveowl.entity.Account;
import com.server.liveowl.payload.request.UpdateInfoRequest;
import com.server.liveowl.payload.request.UploadAvtRequest;
import com.server.liveowl.payload.response.Responsedata;
import com.server.liveowl.payload.request.SingupRequest;
import com.server.liveowl.service.imp.UserServiceImp;
import com.server.liveowl.util.JwtUtilHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    UserServiceImp userServiceImp;

    @Autowired
    JwtUtilHelper jwtUtilHelper;

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestParam String email, @RequestParam String password) {

        Responsedata responsetdata = new Responsedata();

        // tạo key cho api
//        SecretKey key = Jwts.SIG.HS256.key().build(); //or HS384.key() or HS512.key()
//        String secretString = Encoders.BASE64.encode(key.getEncoded());
//        System.out.println(secretString);
       System.out.println(email + ", " + password);
        if(userServiceImp.checkLogin(email, password))
        {
            Account account = userServiceImp.getAccountByEmail(email);
            String userId = account.getAccountId();
            int role = account.getRole();
            String token = jwtUtilHelper.generateToken(email, role);
            responsetdata.setData(new TokenDTO(role,token,userId));
            responsetdata.setMessage("Đăng nhập thành công");
            return new ResponseEntity<>(responsetdata, HttpStatus.OK);
        }
        else
        {
            responsetdata.setData(null);
            responsetdata.setMessage("Đăng nhập không thành công");
            return new ResponseEntity<>(responsetdata, HttpStatus.NOT_FOUND);
        }

    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SingupRequest singupRequest) {
        Responsedata responsetdata = new Responsedata();
        if(userServiceImp.addUser(singupRequest)) {
            responsetdata.setMessage("Đăng kí thành công");
            return new ResponseEntity<>(responsetdata, HttpStatus.OK);
        }
        responsetdata.setMessage("Đăng kí không thành công");
        return new ResponseEntity<>(responsetdata, HttpStatus.NOT_FOUND);
    }

 //   @PreAuthorize("hasAuthority('ROLE_GIAO_VIEN')")
    @PostMapping("/detail")
    public ResponseEntity<?> getDetailUser() {
        Responsedata responsetdata = new Responsedata();

        try {
            // Lấy email từ SecurityContext (đã được xác thực bởi JWT)
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            System.out.println("email controller " + email);

            // Lấy thông tin chi tiết tài khoản
            AccountDetailDTO accountDetail = userServiceImp.getAccountDetail(email);

            if (accountDetail != null) {
                responsetdata.setData(accountDetail);
                responsetdata.setMessage("Lấy thông tin thành công");
                return new ResponseEntity<>(responsetdata, HttpStatus.OK);
            } else {
                responsetdata.setData(null);
                responsetdata.setMessage("Không tìm thấy");
                return new ResponseEntity<>(responsetdata, HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            responsetdata.setData(null);
            responsetdata.setMessage(e.getMessage());
            return new ResponseEntity<>(responsetdata, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/allaccoutid")
    public ResponseEntity<?> getAllAccountID() {
        Responsedata responsetdata = new Responsedata();
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            System.out.println("email controller " + email);
            List<String> allAccountID = userServiceImp.getAllAccountId();
            if(allAccountID != null) {
                responsetdata.setData(allAccountID);
                responsetdata.setMessage("Lấy thông tin thành công");
                return new ResponseEntity<>(responsetdata, HttpStatus.OK);
            }
            else {
                responsetdata.setData(null);
                responsetdata.setMessage("Không tìm thấy");
                return new ResponseEntity<>(responsetdata, HttpStatus.NOT_FOUND);
            }
        }
        catch (Exception e) {
            responsetdata.setData(null);
            responsetdata.setMessage(e.getMessage());
            return new ResponseEntity<>(responsetdata, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/uploadavt")
    public ResponseEntity<?> uploadAVT(@RequestBody UploadAvtRequest uploadAvtRequest)
    {
        String imageBase64 = uploadAvtRequest.getImage();
        Responsedata responsetdata = new Responsedata();
        try
        {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            if(userServiceImp.uploadAVT(email, imageBase64))
            {
                responsetdata.setData(null);
                responsetdata.setMessage("Thay đổi thành công");
                return new ResponseEntity<>(responsetdata, HttpStatus.OK);
            }
            else
            {
                responsetdata.setData(null);
                responsetdata.setMessage("Thay đổi thấy bại");
                return new ResponseEntity<>(responsetdata, HttpStatus.NOT_FOUND);
            }
        }
        catch (Exception e)
        {
            responsetdata.setData(null);
            responsetdata.setMessage(e.getMessage());
            return new ResponseEntity<>(responsetdata, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/updateinfo")
    public ResponseEntity<?> updateInfo(@RequestBody UpdateInfoRequest updateInfoRequest)
    {
        String name = updateInfoRequest.getName();
        String emailnew = updateInfoRequest.getEmail();
        LocalDate dateofbirth = updateInfoRequest.getDateofbirth();
        Boolean gender = updateInfoRequest.getGender();

        Responsedata responsetdata = new Responsedata();
        try
        {
            String emailToken = SecurityContextHolder.getContext().getAuthentication().getName();
            if(userServiceImp.updateInfo(emailToken, name, emailnew, dateofbirth, gender))
            {
                responsetdata.setData(null);
                responsetdata.setMessage("Thay đổi thành công");
                return new ResponseEntity<>(responsetdata, HttpStatus.OK);
            }
            else
            {
                responsetdata.setData(null);
                responsetdata.setMessage("Thay đổi thấy bại");
                return new ResponseEntity<>(responsetdata, HttpStatus.NOT_FOUND);
            }
        }
        catch (Exception e)
        {
            responsetdata.setData(null);
            responsetdata.setMessage(e.getMessage());
            return new ResponseEntity<>(responsetdata, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



}
