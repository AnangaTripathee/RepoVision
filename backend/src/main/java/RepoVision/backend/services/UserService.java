package RepoVision.backend.services;

import java.util.UUID;

import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import RepoVision.backend.entity.User;
import RepoVision.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final TextEncryptor tokenEncryptor;

    @Transactional(readOnly = true)
    public User requiredById(UUID id ){
        return  userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));        
    }

    public String decryptAccessToken(User user){
        return tokenEncryptor.decrypt(user.getAccessToken());

    }


    public static Long toLong(Object value){
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

}