{
  "name": "Mail.com",
  "default_service": true,
  "imap_address": "imap.mail.com",
  "imap_port": 993,
  "inbox_folder": "INBOX",
  "spam_folder": "Spam",
  "domains": [
    "Mail.com"
  ]
}