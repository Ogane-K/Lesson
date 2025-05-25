package com.example.samuraitravel.controller;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.service.HouseService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AdminHouseControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private HouseService houseService;

	@Test
	public void 未ログインの場合は管理者用の民宿一覧ページからログインページにリダイレクトする() throws Exception {
		mockMvc.perform(get("/admin/houses"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"));
	}

	@Test
	@WithUserDetails("taro.samurai@example.com")
	public void 一般ユーザーとしてログイン済みの場合は管理者世の民宿一覧ページが表示されずに403エラーが発生する() throws Exception {
		mockMvc.perform(get("/admin/houses"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("hanako.samurai@example.com")
	public void 管理者としてログイン済みの場合は管理者用の民宿一覧ページが正しく表示される() throws Exception {
		mockMvc.perform(get("/admin/houses"))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/houses/index"));
	}

	@Test
	public void 未ログインの場合は管理者用の民宿詳細ページからログインページにリダイレクトする() throws Exception {
		mockMvc.perform(get("/admin/houses/1"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"));
	}

	@Test
	@WithUserDetails("taro.samurai@example.com")
	public void 一般ユーザーとしてログイン済みの場合は管理者用の民宿詳細ページが表示されずに404エラーが発生する() throws Exception {
		mockMvc.perform(get("/admin/houses/1"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("hanako.samurai@example.com")
	public void 管理者としてログイン済みの場合は管理者用の民宿詳細ページが正しく表示される() throws Exception {
		mockMvc.perform(get("/admin/houses/1"))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/houses/show"));
	}

	@Test
	public void 未ログインの場合は管理者用の民宿登録ページからログインページにリダイレクトする() throws Exception {
		mockMvc.perform(get("/admin/houses/register"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"));

	}

	@Test
	@WithUserDetails("taro.samurai@example.com")
	public void 一般ユーザーとしてログイン済みの場合は管理者用の民宿登録ページが表示されずに404エラーが発生する() throws Exception {
		mockMvc.perform(get("/admin/houses/register"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("hanako.samurai@example.com")
	public void 管理者としてログイン済みの場合は管理者用の民宿登録ページが正しく表示される() throws Exception {
		mockMvc.perform(get("/admin/houses/register"))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/houses/register"));
	}

	@Test
	@Transactional
	public void 未ログインの場合は民宿を登録せずにログインページにリダイレクトする() throws Exception {
		//	テスト前のレコード数を取得する
		long countBefore = houseService.countHouses();

		// テスト用の画像ファイルデータを準備する
		Path filePath = Paths.get("src/main/resources/static/storage/house01.jpg");
		String fileName = filePath.getFileName().toString();
		String fileType = Files.probeContentType(filePath);
		byte[] flieBytes = Files.readAllBytes(filePath);

		MockMultipartFile imageFile = new MockMultipartFile(
				//	フォームのname属性の値
				"imageFile",
				//	ファイル名			
				fileName,
				//	ファイルの形式			
				fileType,
				//	ファイルのバイト配列			
				flieBytes);
		mockMvc.perform(MockMvcRequestBuilders.multipart("/admin/houses/create").file(imageFile)
				.with(csrf())
				.param("name", "テスト民宿名")
				.param("description", "テスト説明")
				.param("price", "5000")
				.param("capasity", "5")
				.param("postalCode", "000-0000")
				.param("address", "テスト住所")
				.param("phoneNumber", "000-000-000"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"));

		long countAfter = houseService.countHouses();

		//	レコード数が変わっていないことを検証する
		assertThat(countAfter).isEqualTo(countBefore);

	}

	@Test
	@Transactional
	@WithUserDetails("taro.samurai@example.com")
	public void 一般ユーザーとしてログイン済みの場合は民宿を登録せずに403エラーが発生する() throws Exception {

		//	テスト前のレコード数を取得
		long countBefore = houseService.countHouses();

		//	テスト用の画像データを準備
		Path filePath = Paths.get("src/main/resources/static/storage/house01.jpg");

		String fileName = filePath.getFileName().toString();
		String fileType = Files.probeContentType(filePath);
		byte[] fileBytes = Files.readAllBytes(filePath);

		MockMultipartFile imageFile = new MockMultipartFile(
				"imageFile",
				fileName,
				fileType,
				fileBytes);

		mockMvc.perform(MockMvcRequestBuilders.multipart("/admin/houses/create").file(imageFile)
				.with(csrf())
				.param("name", "テスト民宿名")
				.param("description", "テスト説明")
				.param("price", "5000")
				.param("capacity", "5")
				.param("postalCode", "000-0000")
				.param("address", "テスト住所")
				.param("phoneNumber", "000-000-000"))
				.andExpect(status().isForbidden());

		//	テスト後のレコード数を取得する	
		long countAfter = houseService.countHouses();

		//	レコード数をテスト前後で比較して、変化しないことを検証
		assertThat(countAfter).isEqualTo(countBefore);
	}

	@Test
	@Transactional
	@WithUserDetails("hanako.samurai@example.com")
	public void 管理者としてログイン済みの場合は民宿登録後に民宿一覧ページにリダイレクトする() throws Exception {
		//		テスト前のレコード数取得
		long countBefore = houseService.countHouses();

		//		テスト用の画像ファイルのデータを用意する
		Path filePath = Paths.get("src/main/resources/static/storage/house01.jpg");
		String fileName = filePath.getFileName().toString();
		String fileType = Files.probeContentType(filePath);
		byte[] fileBytes = Files.readAllBytes(filePath);

		MockMultipartFile imageFile = new MockMultipartFile(
				"imageFile",
				fileName,
				fileType,
				fileBytes);

		mockMvc.perform(MockMvcRequestBuilders.multipart("/admin/houses/create").file(imageFile)
				.with(csrf())
				.param("name", "テスト民宿名")
				.param("description", "テスト説明")
				.param("price", "5000")
				.param("capacity", "5")
				.param("postalCode", "000-0000")
				.param("address", "テスト住所")
				.param("phoneNumber", "000-000-000"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/houses"));

		long countAfter = houseService.countHouses();

		assertThat(countAfter).isEqualTo(countBefore + 1);

		House house = houseService.findFirstHouseByOderByDesc();
		assertThat(house.getName()).isEqualTo("テスト民宿名");
		assertThat(house.getDescription()).isEqualTo("テスト説明");
		assertThat(house.getPrice()).isEqualTo(5000);
		assertThat(house.getCapacity()).isEqualTo(5);
		assertThat(house.getPostalCode()).isEqualTo("000-0000");
		assertThat(house.getAddress()).isEqualTo("テスト住所");
		assertThat(house.getPhoneNumber()).isEqualTo("000-000-000");

	}
	
	@Test
	public void 未ログインの場合は管理者用の民宿編集ページからログインページにリダイレクトする() throws Exception{
		mockMvc.perform(get("/admin/houses/1/edit"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"));
	}
	
	@Test
	@WithUserDetails("taro.samurai@example.com")
	public void 一般ユーザーとしてログイン済みの場合は管理者用の民宿編集ページが表示されずに403エラーが発生する() throws Exception{
		mockMvc.perform(get("/admin/houses/1/edit"))
				.andExpect(status().isForbidden());
	}
	
	@Test
	@WithUserDetails("hanako.samurai@example.com")
	public void 管理者ユーザーとしてログイン済みの場合は管理者用の民宿編集ページが正しく表示される() throws Exception{
		mockMvc.perform(get("/admin/houses/1/edit"))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/houses/edit"));
		
	}
	
}
