import {
  classInvalid,
  classValid,
  emailResetPasswordSelector,
  forgetYourPasswordSelector,
  submitInitResetPasswordSelector,
  usernameLoginSelector,
} from '../../support/commands';

describe('forgot your password', () => {
  const username = Cypress.env('E2E_USERNAME') ?? 'user';

  beforeEach(() => {
    cy.visit('');
    cy.clickOnLoginItem();
    cy.get(usernameLoginSelector).type(username);
    cy.get(forgetYourPasswordSelector).click();
  });

  beforeEach(() => {
    cy.intercept('POST', '/api/account/reset-password/init').as('initResetPassword');
  });

  it('requires email', () => {
    // Clear any existing value first
    cy.get(emailResetPasswordSelector).clear();
    // Try to submit without email to trigger validation
    cy.get(submitInitResetPasswordSelector).click({ force: true });
    // Wait a bit for validation to trigger and check for invalid class
    cy.get(emailResetPasswordSelector).should('have.class', classInvalid);
    // Type a valid email
    cy.get(emailResetPasswordSelector).type('user@gmail.com');
    // Trigger validation by blurring the field
    cy.get(emailResetPasswordSelector).blur();
    // Wait for validation to complete and check for valid class
    cy.get(emailResetPasswordSelector).should('have.class', classValid);
  });

  it('should be able to init reset password', () => {
    cy.get(emailResetPasswordSelector).type('user@gmail.com');
    cy.get(submitInitResetPasswordSelector).click({ force: true });
    cy.wait('@initResetPassword').then(({ response }) => expect(response?.statusCode).to.equal(200));
  });
});
