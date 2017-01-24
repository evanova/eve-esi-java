package org.devfleet.esi;

import java.util.List;

/** Facade toward all ESI APIs.*/
public interface ESIService {

    Character getCharacter(final Long charID);

    Location getCharacterLocation(final Long charID);

    Calendar getCalendar(final Long charID, final Long afterEventID);

    boolean postCalendarEvent(Long charID, Long eventID, Calendar.Event.Response response);

    Corporation getCorporation(final Long corpID);

    List<Corporation.Member> getMembers(final Long corpID);

    // Corporation getCorporation(final long corpID);

    //BookmarkFolder getBookmarks();

    boolean deleteMail(final Long charID, final Long mailID);

    List<Mail> getMails(final Long charID, final Long afterMailID, final String... labels);

    List<Mailbox> getMailboxes(final Long charID);

    Mail getMailContent(final Long charID, final Long mailID);

    Integer postMail(final Long charID, final Mail mail);

    boolean updateMail(final Long charID, final Mail mail);

    boolean createMailbox(final Long charID, final Mailbox mailbox);

    List<KillMail> getKillMails(Long charID, Integer maxCount, Long maxKillID, boolean withContent);

    KillMail getKillMail(final KillMail killMail);
}
