package net.dancier.dancer.dancers;

import lombok.RequiredArgsConstructor;
import net.dancier.dancer.chat.dto.DancerDto;
import net.dancier.dancer.chat.dto.DancerIdsDto;
import net.dancier.dancer.core.dto.PublicProfileDto;
import net.dancier.dancer.core.model.Gender;
import net.dancier.dancer.security.AuthenticatedUser;
import net.dancier.dancer.security.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static net.dancier.dancer.authentication.Constants.ROLE_USER;

@RestController
@RequestMapping("/dancers")
@RequiredArgsConstructor
public class DancerController {
    private final static Logger log = LoggerFactory.getLogger(DancerController.class);

    private final DancerService dancerService;

    @GetMapping("")
    @Secured(ROLE_USER)
    public ResponseEntity<List<PublicProfileDto>> get(
            @CurrentUser AuthenticatedUser authenticatedUser,
            @RequestParam Gender gender,
            @RequestParam(defaultValue = "20") int range
            ) {
        log.info("Fetching list of dancers in {} km range with gender {} for user {}", range, gender, authenticatedUser.getUserId());
        return ResponseEntity.ok(dancerService.getDancerList(authenticatedUser, gender, range));
    }

    @PostMapping("")
    @Secured(ROLE_USER)
    public ResponseEntity<HashMap<UUID, DancerDto>> post(
            @CurrentUser AuthenticatedUser authenticatedUser,
            @RequestBody DancerIdsDto dancerIdsDto) {
        log.info("Fetching list of dancers for user {}.", authenticatedUser.getUserId());
        return ResponseEntity.ok(
                dancerService.getDancerMap(dancerIdsDto)
        );
    }

    @GetMapping("/{dancerId}/mail")
    public ResponseEntity<String> getMail(@PathVariable UUID dancierId) {
        log.info("Getting email-Address for: " + dancierId);
        return ResponseEntity.ok("foo");
    }
}
