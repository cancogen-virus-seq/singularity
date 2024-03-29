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

package org.cancogenvirusseq.singularity.components.model;

import static java.lang.String.format;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.cancogenvirusseq.singularity.components.utils.TsvUtils;
import org.springframework.util.FileSystemUtils;

@Slf4j
@Getter
public class FileBundle {
  public static final String DOWNLOAD_DIR = "/tmp";
  public static final String FILE_NAME_TEMPLATE = "files-archive-";
  public static final String MOLECULAR_PREFIX_FILE_NAME = "2-";
  public static final String METADATA_PREFIX_FILE_NAME = "1-";
  public static final String MOLECULAR_FILE_EXTENSION = ".fasta";
  public static final String METADATA_FILE_EXTENSION = ".tsv";
  public static final String ARCHIVE_EXTENSION = ".tar.gz";

  private final String archiveFilename;
  private final String downloadDirectory;
  private final String molecularFilename;
  private final String metadataFilename;
  private final BufferedOutputStream archiveFileOutputStream;
  private final BufferedOutputStream molecularFileOutputStream;
  private final BufferedOutputStream metadataFileOutputStream;

  @Setter private GzipCompressorOutputStream archiveGzipOutputStream;
  @Setter private TarArchiveOutputStream archiveTarOutputStream;

  @SneakyThrows
  public FileBundle(UUID archiveId) {
    // record archive name and create FileOutputStream (buffered)
    this.archiveFilename = archiveFilenameFromArchiveId(archiveId);
    this.archiveFileOutputStream =
        new BufferedOutputStream(
            new FileOutputStream(format("%s/%s", DOWNLOAD_DIR, this.archiveFilename)));

    // create download directory for file downloads
    this.downloadDirectory = format("%s/%s%s", DOWNLOAD_DIR, FILE_NAME_TEMPLATE, archiveId);

    FileSystemUtils.deleteRecursively(Paths.get(this.downloadDirectory));
    Files.deleteIfExists(Paths.get(this.archiveFilenameFromArchiveId(archiveId)));
    log.info("Cleaning up Download Directory({}) and compressed file({}).", this.downloadDirectory, this.archiveFilenameFromArchiveId(archiveId));
    
    Files.createDirectory(Paths.get(this.downloadDirectory));

    // record molecular filename and create FileOutputStream (buffered)
    this.molecularFilename =
        format("%s%s%s%s", MOLECULAR_PREFIX_FILE_NAME,  FILE_NAME_TEMPLATE, archiveId, MOLECULAR_FILE_EXTENSION);
    this.molecularFileOutputStream =
        new BufferedOutputStream(
            new FileOutputStream(format("%s/%s", this.downloadDirectory, this.molecularFilename)));

    // record metadata filename and create FileOutputStream (buffered)
    this.metadataFilename =
        format("%s%s%s%s", METADATA_PREFIX_FILE_NAME, FILE_NAME_TEMPLATE, archiveId, METADATA_FILE_EXTENSION);
    this.metadataFileOutputStream =
        new BufferedOutputStream(
            new FileOutputStream(format("%s/%s", this.downloadDirectory, this.metadataFilename)));

    // write the tsv header
    this.metadataFileOutputStream.write(TsvUtils.getHeader());
  }

  public static String archiveFilenameFromArchiveId(UUID archiveId) {
    return format("%s%s%s", FILE_NAME_TEMPLATE, archiveId, ARCHIVE_EXTENSION);
  }
}
