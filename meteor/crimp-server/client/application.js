// Client application state

// TOOD: Set default to next/ongoing category
// TODO: Session does not persist, use something else
var category = $.cookie('currentCategory') || 'UMQ';
Session.setDefault('currentCategory', category);
