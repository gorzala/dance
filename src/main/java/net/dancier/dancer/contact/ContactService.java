package net.dancier.dancer.contact;

import lombok.RequiredArgsConstructor;
import net.dancier.dancer.mail.model.DancierMailMessage;
import net.dancier.dancer.mail.service.MailCreationService;
import net.dancier.dancer.mail.service.MailEnqueueService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final MailCreationService mailCreationService;
    private final MailEnqueueService mailEnqueueService;

    void send(ContactDto contactDto) {
        DancierMailMessage mailToSender = mailCreationService.createDancierMessageFromTemplate(
                contactDto.getSender(),
                "dev@dancier.net",
                "Vielen Dank - Team Dancier",
                MailCreationService.CONTACT_FORMULAR_FEEDBACK,
                Map.of());

        mailEnqueueService.enqueueMail(mailToSender);
        DancierMailMessage mailToTeamDancier = mailCreationService.createDancierMessageFromTemplate(
                "dev@dancier.net",
                contactDto.getSender(),
                "Mail über das Kontakt formular",
                MailCreationService.CONTACT_FORMULAR,
                Map.of(
                "sender", contactDto.getSender(),
                "message", contactDto.getMessage())
                );
        mailEnqueueService.enqueueMail(mailToTeamDancier);
    }

}
