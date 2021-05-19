/*
 * Copyright (c) 2021 The Ontario Institute for Cancer Research. All rights reserved
 *
 * This program and the accompanying materials are made available under the terms of the GNU Affero General Public License v3.0.
 * You should have received a copy of the GNU Affero General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.cancogenvirusseq.all.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.cancogenvirusseq.all.api.model.EntityListResponse;
import org.cancogenvirusseq.all.api.model.ErrorResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import reactor.core.publisher.Mono;

@Api(value = "All Contributors, All Files, All", tags = "All")
public interface ApiDefinition {
  String UNKNOWN_MSG = "An unexpected error occurred.";

  @ApiOperation(
      value = "Get All Contributors",
      nickname = "Get Contributors",
      response = EntityListResponse.class,
      tags = "All")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "", response = EntityListResponse.class),
        @ApiResponse(code = 500, message = UNKNOWN_MSG, response = ErrorResponse.class)
      })
  @RequestMapping(
      value = "/contributors",
      produces = MediaType.APPLICATION_JSON_VALUE,
      method = RequestMethod.GET)
  Mono<EntityListResponse<String>> getContributors();

  @ApiOperation(
      value = "Download all molecular files as a single .fasta.gz gzip compressed file",
      nickname = "Download Files",
      response = String.class,
      tags = "All")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "", response = String.class),
        @ApiResponse(code = 500, message = UNKNOWN_MSG, response = ErrorResponse.class)
      })
  @RequestMapping(
      value = "/files",
      produces = MediaType.TEXT_PLAIN_VALUE,
      method = RequestMethod.GET)
  Mono<String> getFiles();

  //  @ApiOperation(
  //      value = "Download all molecular files as a single .fasta.gz gzip compressed file",
  //      nickname = "Download Files",
  //      response = MultipartFile.class,
  //      tags = "All")
  //  @ApiResponses(
  //      value = {
  //        @ApiResponse(code = 200, message = "", response = MultipartFile.class),
  //        @ApiResponse(code = 500, message = UNKNOWN_MSG, response = ErrorResponse.class)
  //      })
  //  @RequestMapping(
  //      value = "/files",
  //      produces = MediaType.APPLICATION_OCTET_STREAM_VALUE,
  //      method = RequestMethod.GET)
  //  ResponseEntity<Mono<DataBuffer>> download();
}
