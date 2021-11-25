package net.dancier.dancer.controller;

import net.dancier.dancer.DancerDto;
import net.dancier.dancer.DancerService;
import net.dancier.dancer.model.Dancer;
import net.dancier.dancer.security.CurrentUser;
import net.dancier.dancer.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/dancer")
public class DancerController {

    @Autowired
    DancerService dancerService;

    @GetMapping
    public ResponseEntity get(@CurrentUser UserPrincipal userPrincipal) {
        DancerDto dancerDto = dancerService.getDancerById(userPrincipal.getId());
        return ResponseEntity.ok(dancerDto);
    }

}
