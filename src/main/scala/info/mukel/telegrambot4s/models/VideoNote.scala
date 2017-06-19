package info.mukel.telegrambot4s.models

/**
  * This object represents a video message (available in Telegram apps as of v.4.0).

  * @param fileId    String Unique identifier for this file
  * @param length    Integer Video width and height as defined by sender
  * @param duration  Integer Duration of the video in seconds as defined by sender
  * @param thumb     PhotoSize Optional. Video thumbnail
  * @param fileSize  Integer Optional. File size
  */
case class VideoNote(
                    fileId   : String,
                    length   : Int,
                    duration : Int,
                    thumb    : Option[PhotoSize] = None,
                    fileSize : Option[Int] = None
                    )