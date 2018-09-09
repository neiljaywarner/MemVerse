package com.spiritflightapps.memverse.model

data class User(val id: Int, val created_at: String, val remember_token_expires_at: String, val device_type: String)
// eg 2018-09-09 18:25:17.813 19449-19499/com.spiritflightapps.memverse.debug D/OkHttp: {"response":{"id":51836,"login":"neiltest1","identity_url":null,"name":"neiltest1","email":"neiltest1@mailinator.com","remember_token_expires_at":null,"deleted_at":null,"created_at":"2018-09-09T23:25:17.000Z","updated_at":"2018-09-09T23:25:17.000Z","last_reminder":null,"reminder_freq":"weekly","newsletters":true,"church_id":null,"country_id":null,"language":"English","time_allocation":5,"memorized":0,"learning":0,"last_activity_date":null,"show_echo":true,"max_interval":366,"mnemonic_use":"Learning","american_state_id":null,"accuracy":10,"all_refs":true,"rank":null,"ref_grade":10,"gender":null,"translation":null,"level":0,"referred_by":null,"show_email":false,"auto_work_load":true,"admin":false,"group_id":null,"thredded_admin":false,"forem_state":"pending_review","forem_auto_subscribe":false,"provider":null,"uid":null,"sync_subsections":false,"quiz_alert":false,"device_token":null,"device_type":null,"time_zone":"UTC"}}
/*
{
  "id": 0,
  "login": "string",
  "identity_url": "string",
  "name": "string",
  "email": "string",
  "remember_token_expires_at": "string",
  "deleted_at": "string",
  "created_at": "string",
  "updated_at": "string",
  "last_reminder": "string",
  "reminder_freq": "string",
  "newsletters": true,
  "church_id": 0,
  "country_id": 0,
  "language": "string",
  "time_allocation": 0,
  "memorized": 0,
  "learning": 0,
  "last_activity_date": "string",
  "show_echo": true,
  "max_interval": 0,
  "mnemonic_use": "string",
  "american_state_id": 0,
  "accuracy": 0,
  "all_refs": true,
  "rank": 0,
  "ref_grade": 0,
  "gender": "string",
  "translation": "string",
  "level": 0,
  "referred_by": 0,
  "show_email": true,
  "auto_work_load": true,
  "admin": true,
  "group_id": 0,
  "provider": "string",
  "uid": "string",
  "sync_subsections": true,
  "quiz_alert": true,
  "device_token": "string",
  "device_type": "string"
}


 */