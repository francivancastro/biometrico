package com.demo.springbootbiometrico.services;

import com.demo.springbootbiometrico.models.UserDetails;
import com.machinezoo.sourceafis.FingerprintMatcher;
import com.machinezoo.sourceafis.FingerprintTemplate;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@CacheConfig(cacheNames={"users"})
public class UserService {

    private List<UserDetails> users = new ArrayList<>();

    @Autowired
    UserService() {}

    @PostConstruct
    private void fillUsers(){
        byte[] ISOminutiaeBuffer2;
        List<String> conteudo;
        try {
            conteudo = Files.readAllLines(Paths.get("/var/www/scoli/public/biometria/biometria.txt"));
            for(String b : conteudo){
                //pega inicio ate antes do @
                int id = Integer.parseInt(b.substring(0 , b.indexOf("@")));
                String nome = b.substring(0 , b.indexOf("@"));
                //pega apos @ ate o fim 
                String base64 = b.substring(b.indexOf("@") + 1);
                
                ISOminutiaeBuffer2 = Base64.getDecoder().decode(base64);
                UserDetails user = new UserDetails(id, nome,  new FingerprintTemplate().convert(ISOminutiaeBuffer2));
                this.users.add(user);
                getUser(user);
            }
        } catch (IOException ex) {
            Logger.getLogger(UserService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public List<UserDetails> findAll() {
        return this.users;
    }
    
    // isso que adiciona na cachce
    @Cacheable(key = "#user.id")
    public int getUser(UserDetails user){
        int posicao = getPosition(user);
        return posicao;
    }
    
    public UserDetails insertUser(UserDetails user){
        this.users.add(user);
        getUser(user);
        return user;
    }

    @CachePut(key = "#user.id")
    public UserDetails updateUser(UserDetails user){
        ///////////////////////////
        // edita posição do array//
        ///////////////////////////
        // procura a posicao do usuario no array
        UserDetails posicao = getIdUser(user.id);
        // edita o usuario no array
        /// fazer editar na cache
        this.users.set(posicao.id, user);
        return this.users.get(posicao.id);
    }
    
    
    @CacheEvict(key = "#id")
    public List<UserDetails> deleteUser(int id) {
        UserDetails user = getIdUser(id);
        this.users.remove(user);
        return this.users;
    }
    
    public int getPosition(UserDetails user){
        return this.users.indexOf(user);        
    }
    
    public UserDetails findInN(FingerprintTemplate probe, Iterable<UserDetails> candidates) {
        FingerprintMatcher matcher = new FingerprintMatcher() .index(probe);
        UserDetails match = null;
        double high = 0;
        for (UserDetails candidate : candidates) {
            double score = matcher.match(candidate.template);
            // A condição padrão buscava em todas as biometrias (score > high)
            // Mudamos a condição para (score > 39), isso pega a primeira biometria encontrada;
            // VOLTAMOS AO PADRÃO <--> BUSCAR EM TODAS A MAIOR
            if (score > high) {
                high = score;
                match = candidate;
            }
           // System.out.println("math " + candidate.id + " with score: " + score);
        }
         System.out.println(" Ponto: " + high);
        double threshold = 45;
        return high >= threshold ? match : null;
    }
    
    public UserDetails getIdUser(int id){
        for(UserDetails user : this.users){
            if(id == user.id){
                return user;
            }
        }
        return null;
    }
}
