package me.demo.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.approval.Approval;
import org.springframework.security.oauth2.provider.approval.Approval.ApprovalStatus;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.servlet.support.csrf.CsrfRequestDataValueProcessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Remove this controller to use the Whitelabel implementation of the ApprovalEndpoint.
 */
@Controller
@SessionAttributes(AccessConfirmationController.AUTHORIZATION_REQUEST)
@RequiredArgsConstructor
public class AccessConfirmationController {

	static final String AUTHORIZATION_REQUEST = "authorizationRequest";
	static final String CSRF = "_csrf";

	private final ClientDetailsService clientDetailsService;

	private final ApprovalStore approvalStore;

	@RequestMapping("/oauth/confirm_access")
	public String getAccessConfirmation(HttpSession session, HttpServletRequest request) throws Exception {
		if (request.getAttribute(CSRF) != null) {
			session.setAttribute(CSRF, request.getAttribute(CSRF));
		}

		// alternatively, can return a jsp page here (assuming JSP resolver and MVC are configured appropriately)
		return "forward:/access_confirmation.html";
	}

	@ResponseBody
	@RequestMapping("/client-confirm-info")
	public Map<String, Object> test(HttpSession session, Principal principal) {
		Map<String, Object> response = new LinkedHashMap<>();

		CsrfToken csrfToken = (CsrfToken) session.getAttribute(CSRF);
		if (csrfToken != null) {
			response.put("csrfParamName", HtmlUtils.htmlEscape(csrfToken.getParameterName()));
			response.put("csrfToken", HtmlUtils.htmlEscape(csrfToken.getToken()));
			session.removeAttribute(CSRF);
		}

		AuthorizationRequest clientAuth = (AuthorizationRequest) session.getAttribute(AUTHORIZATION_REQUEST);
		session.removeAttribute(AUTHORIZATION_REQUEST);

		ClientDetails client = clientDetailsService.loadClientByClientId(clientAuth.getClientId());


		response.put("clientId", client.getClientId());

		Map<String, String> scopes = new LinkedHashMap<>();
		for (String scope : clientAuth.getScope()) {
			scopes.put(OAuth2Utils.SCOPE_PREFIX + scope, "false");
		}
		for (Approval approval : approvalStore.getApprovals(principal.getName(), client.getClientId())) {
			if (clientAuth.getScope().contains(approval.getScope())) {
				scopes.put(OAuth2Utils.SCOPE_PREFIX + approval.getScope(),
						approval.getStatus() == ApprovalStatus.APPROVED ? "true" : "false");
			}
		}
		response.put("scopes", scopes);

		return response;
	}

}


