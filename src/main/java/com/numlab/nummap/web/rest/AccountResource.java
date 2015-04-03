package com.numlab.nummap.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.numlab.nummap.domain.*;
import com.numlab.nummap.domain.enumerations.CategoryEnum;
import com.numlab.nummap.repository.PersistentTokenRepository;
import com.numlab.nummap.repository.UserRepository;
import com.numlab.nummap.security.AuthoritiesConstants;
import com.numlab.nummap.security.SecurityUtils;
import com.numlab.nummap.service.LocationService;
import com.numlab.nummap.service.MailService;
import com.numlab.nummap.service.UserService;
import com.numlab.nummap.web.rest.dto.UserDTO;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/api")
public class AccountResource {

    private final Logger log = LoggerFactory.getLogger(AccountResource.class);

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserService userService;

    @Inject
    private PersistentTokenRepository persistentTokenRepository;

    @Inject
    private MailService mailService;

    @Inject
    private LocationService locationService;



    /**
     * POST  /register -> register the user.
     */
    @RequestMapping(value = "/register",
            method = RequestMethod.POST,
            produces = MediaType.TEXT_PLAIN_VALUE)
    @Timed
    public ResponseEntity<?> registerAccount(@Valid @RequestBody String json, HttpServletRequest request) {
        /* Parsing du paramètre en UserDTO */
        UserDTO userDTO = UserDTO.fromJsonToUserDTO(json);

        /* Récupération de l'adresse */
        Address address ;
        if (CategoryEnum.COMPANY.equals(userDTO.getCategory())){
            address = userDTO.getCompanyContactInformation().getAddress();
        } else {
            address = userDTO.getPersonContactInformation().getAddress();
        }

        /* Récupération des coordonnées */
        Location location = locationService.getLocationFromAddress(address);


        return userRepository.findOneByLogin(userDTO.getLogin())
            .map(user -> new ResponseEntity<>("login already in use", HttpStatus.BAD_REQUEST))
            .orElseGet(() -> userRepository.findOneByEmail(userDTO.getEmail())
                .map(user -> new ResponseEntity<>("e-mail address already in use", HttpStatus.BAD_REQUEST))
                .orElseGet(() -> {
                    User user = userService.createUserInformation(
                        userDTO.getLogin(),
                        userDTO.getPassword(),
                        userDTO.getEmail().toLowerCase(),
                        userDTO.getLocation(),
                        userDTO.getCategory(),
                        userDTO.getDescription(),
                        userDTO.getRaisonSociale(),
                        userDTO.getPersonContactInformation(),
                        userDTO.getCompanyContactInformation(),
                        userDTO.getCompetencies(),
                        userDTO.getSectors(),
                        userDTO.getFields(),
                        userDTO.getLangKey());
                    String baseUrl = request.getScheme() + // "http"
                    "://" +                                // "://"
                    request.getServerName() +              // "myhost"
                    ":" +                                  // ":"
                    request.getServerPort();               // "80"

                    mailService.sendActivationEmail(user, baseUrl);
                    return new ResponseEntity<>(HttpStatus.CREATED);
                })
        );
    }
    /**
     * GET  /activate -> activate the registered user.
     */
    @RequestMapping(value = "/activate",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<String> activateAccount(@RequestParam(value = "key") String key) {
        return Optional.ofNullable(userService.activateRegistration(key))
            .map(user -> new ResponseEntity<String>(HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * GET  /authenticate -> check if the user is authenticated, and return its login.
     */
    @RequestMapping(value = "/authenticate",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public String isAuthenticated(HttpServletRequest request) {
        log.debug("REST request to check if the current user is authenticated");
        return request.getRemoteUser();
    }

    /**
     * GET  /account -> get the current user.
     */
    @RequestMapping(value = "/account",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<UserDTO> getAccount() {
        return Optional.ofNullable(userService.getUserWithAuthorities())
            .map(user -> new ResponseEntity<>(
                new UserDTO(
                    user.getLogin(),
                    null,
                    user.getEmail(),
                    user.getCategory(),
                    user.getDescription(),
                    null,
                    user.getPersonContactInformation(),
                    user.getCompanyContactInformation(),
                    user.getCompetencies(),
                    user.getSectors(),
                    user.getFields(),
                    user.getSiren(),
                    user.getLangKey(),
                    user.getAuthorities().stream().map(Authority::getName).collect(Collectors.toList())),
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * POST  /account -> update the current user information.
     */
    @RequestMapping(value = "/account",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<String> saveAccount(@RequestBody UserDTO userDTO) {
        return userRepository
            .findOneByLogin(userDTO.getLogin())
            .filter(u -> u.getLogin().equals(SecurityUtils.getCurrentLogin()))
            .map(u -> {
                userService.updateUserInformation(userDTO.  getEmail(),
                                                    userDTO.getCategory(),
                                                    userDTO.getDescription(),
                                                    userDTO.getRaisonSociale(),
                                                    userDTO.getPersonContactInformation(),
                                                    userDTO.getCompanyContactInformation(),
                                                    userDTO.getCompetencies(),
                                                    userDTO.getSectors(),
                                                    userDTO.getFields());
                return new ResponseEntity<String>(HttpStatus.OK);
            })
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }


    /**
     * POST  /accountmanagement -> update the user information identified by login.
     */
    @RequestMapping(value = "/accountmanagement",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    @Timed
    public ResponseEntity<String> updateAccount(@RequestBody UserDTO userDTO) {
        return userRepository
                .findOneByLogin(userDTO.getLogin())
                .filter(u -> u.getLogin().equals(userDTO.getLogin()))
                .map(u -> {
                    userService.updateUserByAdminInformation(userDTO.getLogin(),
                            userDTO.getEmail(),
                            userDTO.getCategory(),
                            userDTO.getDescription(),
                            userDTO.getRaisonSociale(),
                            userDTO.getPersonContactInformation(),
                            userDTO.getCompanyContactInformation(),
                            userDTO.getCompetencies(),
                            userDTO.getSectors(),
                            userDTO.getFields());
                    return new ResponseEntity<String>(HttpStatus.OK);
                })
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }


    /**
     * POST  /change_password -> changes the current user's password
     */
    @RequestMapping(value = "/account/change_password",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<?> changePassword(@RequestBody String password) {
        if (StringUtils.isEmpty(password)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        userService.changePassword(password);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * GET  /account/sessions -> get the current open sessions.
     */
    @RequestMapping(value = "/account/sessions",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<PersistentToken>> getCurrentSessions() {
        return userRepository.findOneByLogin(SecurityUtils.getCurrentLogin())
            .map(user -> new ResponseEntity<>(
                persistentTokenRepository.findByUser(user),
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * DELETE  /account/sessions?series={series} -> invalidate an existing session.
     *
     * - You can only delete your own sessions, not any other user's session
     * - If you delete one of your existing sessions, and that you are currently logged in on that session, you will
     *   still be able to use that session, until you quit your browser: it does not work in real time (there is
     *   no API for that), it only removes the "remember me" cookie
     * - This is also true if you invalidate your current session: you will still be able to use it until you close
     *   your browser or that the session times out. But automatic login (the "remember me" cookie) will not work
     *   anymore.
     *   There is an API to invalidate the current session, but there is no API to check which session uses which
     *   cookie.
     */
    @RequestMapping(value = "/account/sessions/{series}",
            method = RequestMethod.DELETE)
    @Timed
    public void invalidateSession(@PathVariable String series) throws UnsupportedEncodingException {
        String decodedSeries = URLDecoder.decode(series, "UTF-8");
        userRepository.findOneByLogin(SecurityUtils.getCurrentLogin()).ifPresent(u -> {
            persistentTokenRepository.findByUser(u).stream()
                .filter(persistentToken -> StringUtils.equals(persistentToken.getSeries(), decodedSeries))
                .findAny().ifPresent(t -> persistentTokenRepository.delete(decodedSeries));
        });
    }


    /**
     * POST /reset -> reset the password
     */
    @RequestMapping(value = "/reset/{loginoremail}",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<?> resetPassword(@PathVariable String loginoremail, HttpServletRequest request){
        log.debug("Rest request to reset the password for user with : {}", loginoremail);
        String baseUrl = request.getScheme() + // "http"
                "://" +                                // "://"
                request.getServerName() +              // "myhost"
                ":" +                                  // ":"
                request.getServerPort();               // "80"

        return(userService.sendresetKey(loginoremail, baseUrl));
    }



    /**
     * GET /reset -> change the password
     */
    @RequestMapping(value = "/resetkey/{resetkey}/{newpassword}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public void resetPassword(@PathVariable("resetkey") String resetkey, @PathVariable("newpassword") String newpassword, HttpServletResponse response){
        log.debug("Rest request to reset the password for user with : {}", newpassword);
        ResponseEntity<?> responseEntity = userService.resetPassword(resetkey, newpassword);
       // System.out.println("RESPONSE ENTITY"+responseEntity.getStatusCode());
    }


}
