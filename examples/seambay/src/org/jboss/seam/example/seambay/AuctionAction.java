package org.jboss.seam.example.seambay;

import static org.jboss.seam.ScopeType.CONVERSATION;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

@Scope(CONVERSATION)
@Name("auctionAction")
@Restrict("#{identity.loggedIn}")
public class AuctionAction implements Serializable
{
   private static final long serialVersionUID = -6738397725125671313L;
   
   @In EntityManager entityManager;
   
   @In User authenticatedUser;

   private Auction auction;
   
   private int durationDays;

   @Begin
   public void createAuction()
   {
      auction = new Auction();
      auction.setUser(authenticatedUser);
      
      //auction.setStatus(Auction.STATUS_UNLISTED);
      
      // temporary
      auction.setStatus(Auction.STATUS_LIVE);
      auction.setEndDate(new Date(System.currentTimeMillis() + (1000 * 60 * 60 * 24 * 7)));
      
      entityManager.persist(auction);     
   }   
      
   @Begin(join = true)
   public void editAuction(Integer auctionId)
   {
      auction = entityManager.find(Auction.class, auctionId);
   }
   
   public void setDetails(String title, String description, int categoryId)
   {
      auction.setTitle(title);
      auction.setDescription(description);
      auction.setCategory(entityManager.find(Category.class, categoryId));      
   }
   
   public void setDuration(int days)
   {
      this.durationDays = days;
   }
   
   @End
   public void confirm()
   {
      Calendar cal = new GregorianCalendar(); 
      cal.add(Calendar.DAY_OF_MONTH, durationDays);
      auction.setEndDate(cal.getTime());
      auction.setStatus(Auction.STATUS_LIVE);
      auction = entityManager.merge(auction);
   }

   public Auction getAuction()
   {
      return auction;
   }
}
