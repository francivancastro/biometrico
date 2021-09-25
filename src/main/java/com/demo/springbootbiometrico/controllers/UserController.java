package com.demo.springbootbiometrico.controllers;

import com.demo.springbootbiometrico.models.UserDetails;
import com.demo.springbootbiometrico.services.UserService;
import com.machinezoo.sourceafis.FingerprintTemplate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/users")
public class UserController {

    private final UserService userService;

    @Autowired
    UserController (UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = "/all")
    public List<String> getAllUsers() {
        List<String> biometrias = new ArrayList<String>();
        for(UserDetails user : userService.findAll()){
            biometrias.add("{'"+user.id+"' : '"+user.name+"'}");
        }
        return biometrias;
    }
    
    //@GetMapping(path = "/comparar/{string}")
    // @RequestMapping(method=RequestMethod.POST, value="/comparar")
    @PostMapping(value = "/comparar")
    public @ResponseBody String matching1toN(@RequestBody String string) {
        System.out.println("-----START 1:N matching-----");
        //System.out.println(string);
        byte[] ISOminutiaeBuffer1;
        String hash = string.replace("%2F", "/");
        String base64 = hash.substring (0, hash.length() - 1);
        ISOminutiaeBuffer1 =  Base64.getDecoder().decode(base64);
        FingerprintTemplate probe = new FingerprintTemplate ()
            .convert(ISOminutiaeBuffer1);
        
        long start = System.currentTimeMillis();
        UserDetails match = userService.findInN(probe, userService.findAll());
        System.out.println("Result: " + (match == null ? "Not found" : match.name));
        System.out.println("-----Tempo de Comparação " + (System.currentTimeMillis() - start) + "ms -----");
        return (match == null ? "Not found" : match.name);
    }

    @PostMapping(value = "/insert")
    public UserDetails insert(@RequestBody String string) {
        String hash = string.replace("%2F", "/").replace("%40", "@");
        int id = Integer.parseInt(hash.substring(0 , hash.indexOf("@")));
        String nome = hash.substring(0 , hash.indexOf("@"));
        String s = hash.substring(hash.indexOf("@") + 1);
        String base64 = s.substring (0, s.length() - 1);
        byte[] ISOminutiaeBuffer2 = Base64.getDecoder().decode(base64);
        UserDetails user = new UserDetails(id, nome,  new FingerprintTemplate().convert(ISOminutiaeBuffer2));
        return userService.insertUser(user);
    }
    
    @PostMapping(value = "/update")
    public UserDetails update(@RequestBody String string) {
        String hash = string.replace("%2F", "/").replace("%40", "@");
        int id = Integer.parseInt(hash.substring(0 , hash.indexOf("@")));
        String nome = hash.substring(0 , hash.indexOf("@"));
        String base64 = hash.substring(hash.indexOf("@") + 1);
        byte[] ISOminutiaeBuffer2 = Base64.getDecoder().decode(base64);
        UserDetails user = new UserDetails(id, nome,  new FingerprintTemplate().convert(ISOminutiaeBuffer2));
        return userService.updateUser(user);
    }
    
    @PostMapping(value = "/excluir")
    public List<UserDetails> excluir(@RequestBody String string){
        int tamanho = string.length();
        string = string.substring(0, tamanho-1);
        int id = Integer.parseInt(string);
        return userService.deleteUser(id);
    }
}
