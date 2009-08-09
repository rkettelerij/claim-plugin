package hudson.plugins.claim;

import hudson.model.BuildBadgeAction;
import hudson.model.Hudson;
import hudson.model.ProminentProjectAction;
import hudson.model.Saveable;

import java.io.IOException;

import javax.servlet.ServletException;

import org.acegisecurity.Authentication;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public abstract class AbstractClaimBuildAction<T extends Saveable> implements BuildBadgeAction,
		ProminentProjectAction {

	private static final long serialVersionUID = 1L;

	private boolean claimed;
	private String claimedBy;
	private boolean transientClaim;
	
	protected T owner;
	
	AbstractClaimBuildAction(T owner) {
		this.owner = owner;
	}

	private String reason;

	public String getIconFileName() {
		return null;
	}

	public String getUrlName() {
		return "claim";
	}

	public void doClaim(StaplerRequest req, StaplerResponse resp)
			throws ServletException, IOException {
		Authentication authentication = Hudson.getAuthentication();
		String name = authentication.getName();
		String reason = (String) req.getSubmittedForm().get("reason");
		boolean sticky = req.getSubmittedForm().getBoolean("sticky");
		if (StringUtils.isEmpty(reason)) reason = null;
		claim(name, reason, sticky);
		owner.save();
		resp.forwardToPreviousPage(req);
	}

	public void doUnclaim(StaplerRequest req, StaplerResponse resp)
			throws ServletException, IOException {
		unclaim();
		owner.save();
		resp.forwardToPreviousPage(req);
	}

	public String getClaimedBy() {
		return claimedBy;
	}

	public void setClaimedBy(String claimedBy) {
		this.claimedBy = claimedBy;
	}

	public boolean isClaimed() {
		return claimed;
	}

	public void claim(String claimedBy, String reason, boolean sticky) {
		this.claimed = true;
		this.claimedBy = claimedBy;
		this.reason = reason;
		this.transientClaim = !sticky;
	}
	
	/**
	 * Claim a new Run with the same settings as this one.
	 */
	public void copyTo(AbstractClaimBuildAction other) {
		other.claim(getClaimedBy(), getReason(), isSticky());
	}

	public void unclaim() {
		this.claimed = false;
		this.claimedBy = null;
		this.transientClaim = false;
		// we remember the reason to show it if someone reclaims this build.
	}

	public boolean isClaimedByMe() {
		return !isUserAnonymous()
				&& Hudson.getAuthentication().getName().equals(claimedBy);
	}

	public boolean canClaim() {
		return !isUserAnonymous() && !isClaimedByMe();
	}

	public boolean canRelease() {
		return !isUserAnonymous() && isClaimedByMe();
	}

	public boolean isUserAnonymous() {
		return Hudson.getAuthentication().getName().equals("anonymous");
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public boolean hasReason() {
		return !StringUtils.isEmpty(reason);
	}

	public boolean isTransient() {
		return transientClaim;
	}

	public void setTransient(boolean transientClaim) {
		this.transientClaim = transientClaim;
	}
	
	public boolean isSticky() {
		return !transientClaim;
	}
	
	public void setSticky(boolean sticky) {
		this.transientClaim = !sticky;
	}
	
	public abstract String getNoun();

}