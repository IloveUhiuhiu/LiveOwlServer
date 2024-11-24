package com.server.liveowl.service;

import com.server.liveowl.dto.AccountDetailDTO;
import com.server.liveowl.dto.UserDTO;
import com.server.liveowl.entity.Account;
import com.server.liveowl.entity.AccountInfor;
import com.server.liveowl.payload.request.SingupRequest;
import com.server.liveowl.repository.AccountInforRepository;
import com.server.liveowl.repository.UserReposiroty;
import com.server.liveowl.service.imp.UserServiceImp;
import com.server.liveowl.util.BlobConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.sql.rowset.serial.SerialBlob;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class UserService implements UserServiceImp {
    @Autowired
    UserReposiroty accountReposiroty;

    @Autowired
    AccountInforRepository accountInforRepository;


    @Override
    public List<String> getAllAccountId()
    {
        List<Account> allAccount = accountReposiroty.findAll();
        List<String> allAccountId = new ArrayList<>();
        for (Account account : allAccount)
        {
            allAccountId.add(account.getAccountId());
        }
        return allAccountId;
    }

    @Override
    public Boolean checkLogin(String email, String rawPassword)
    {
        List<Account> listaccount = accountReposiroty.findByEmail(email);
        if (!listaccount.isEmpty())
        {
            Account account = listaccount.get(0);
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            if (passwordEncoder.matches(rawPassword, account.getPassword()))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public Boolean addUser(SingupRequest singupRequest)
    {
        String password = singupRequest.getPassword();
        String email = singupRequest.getEmail();
        String fullname = singupRequest.getFullname();
        LocalDate dateofbirth = singupRequest.getDateofbirth();
        int role = singupRequest.getRole();
        Boolean gender = singupRequest.getGender();

        List<Account> listaccount = accountReposiroty.findByEmail(email);

        if (listaccount.size() == 0)
        {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String hashedPassword = passwordEncoder.encode(password);
            String accountId = UUID.randomUUID().toString().substring(0, 8);
            Blob default_avt = null;
            try (InputStream inputStream = getClass().getResourceAsStream("/image/default_avt.png");
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                if (inputStream == null) {
                    throw new Exception("Image not found");
                }
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                byte[] imageBytes = baos.toByteArray(); // Chuyển đổi thành mảng byte
                default_avt = new SerialBlob(imageBytes);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            Account account = new Account(accountId, email, hashedPassword, role);
            AccountInfor accountInfor = new AccountInfor(accountId, fullname, dateofbirth, gender, default_avt);

            accountInfor.setAccount(account);  // Liên kết AccountInfor với Account
            account.setAccountInfor(accountInfor);  // Liên kết Account với AccountInfor
            accountReposiroty.save(account);  // Lưu account vào cơ sở dữ liệu
            return true;
        }
        return false;
    }


    public Account getAccountByEmail(String email)
    {
        List<Account> listaccount = accountReposiroty.findByEmail(email);
        if (listaccount.isEmpty())
        {
            return null;
        }
        return listaccount.get(0);
    }
    @Override
    public int getUserRole(String email)
    {
        int role = accountReposiroty.findByEmail(email).get(0).getRole();
        return role;
    }

    @Override
    public AccountDetailDTO getAccountDetail(String email) {
        List<Account> account = accountReposiroty.findByEmail(email);
        if (account != null && !account.isEmpty())
        {
            AccountInfor accountInfor = accountInforRepository.findByAccountId(account.get(0).getAccountId());
            BlobConverter b = new BlobConverter();
            String profile = null;
            try
            {
                profile = b.blobToBase64(accountInfor.getProfile());
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
            return new AccountDetailDTO(
                    account.get(0).getAccountId(),
                    account.get(0).getEmail(),
                    account.get(0).getRole(),
                    accountInfor.getFullName(),
                    accountInfor.getDateOfBirth(),
                    accountInfor.getGender(),
                    profile,
                    accountInfor.getCreateAt(),
                    accountInfor.getUpdateAt());
        }
        else
        {
            return null;
        }
    }

    @Override
    public Account getAccountById(String accountId) {
        return accountReposiroty.findByAccountId(accountId);
    }


}
