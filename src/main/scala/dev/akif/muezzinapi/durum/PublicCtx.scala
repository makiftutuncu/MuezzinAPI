package dev.akif.muezzinapi.durum

import dev.akif.durum.Ctx

final case class PublicCtx[B](override val id: String,
                              override val time: Long,
                              override val request: Req,
                              override val headers: Map[String, String],
                              override val body: B) extends Ctx[Req, B, Unit](id, time, request, headers, body, ())
